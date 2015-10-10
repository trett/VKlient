/*
 * (C) Copyright Tretyakov Roman.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package ru.trett.vkapi;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import org.json.JSONObject;
import ru.trett.vkapi.Exceptions.RequestReturnErrorException;
import ru.trett.vkapi.Exceptions.RequestReturnNullException;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Account extends Buddy {

    public BooleanProperty authFinished = new SimpleBooleanProperty(false);
    private String accessToken;
    private ArrayList<Buddy> friends;
    private ScheduledExecutorService scheduledTimer;
    private Runnable stopTimer;
    private OnlineStatus onlineStatus;
    private LongPollServer longPollServer;
    private NetworkHelper networkHelper = new NetworkHelper();
    private boolean errorState;

    /**
     * Create account with given userId and Access token and connect to Long Poll Server
     *
     * @param userId      int user_id
     * @param accessToken String access_token
     */
    public void create(final int userId, final String accessToken) {
        setUserId(userId);
        this.accessToken = accessToken;
        ArrayList<Buddy> buddies = new Users().get(new ArrayList<Integer>(1) {{
            add(userId);
        }}, accessToken);
        if (buddies == null)
            throw new RuntimeException("Impossible to create account");
        Buddy me = buddies.get(0);
        setFirstName(me.getFirstName());
        setLastName(me.getLastName());
        setStatus(me.getStatus());
        setAvatarURL(me.getAvatarURL());
        longPollServer = new LongPollServer(this);
    }

    @Override
    public OnlineStatus getOnlineStatus() {
        return this.onlineStatus;
    }

    @Override
    public void setOnlineStatus(OnlineStatus onlineStatus, OnlineStatusReason reason) {
        this.onlineStatus = onlineStatus;
        System.out.println("Account set status " + onlineStatus.name() +
                " Reason: " + reason.name());
        try {
            switch (onlineStatus) {
                case ONLINE:
                    longPollServer.start();
                    scheduledTimer = Executors.newSingleThreadScheduledExecutor();
                    ScheduledFuture<?> scheduledFuture = scheduledTimer.scheduleAtFixedRate(
                            () -> {
                                try {
                                    setOnline();
                                } catch (RequestReturnNullException | RequestReturnErrorException e) {
                                    connectionError(e);
                                }
                            }, 5, 900, TimeUnit.SECONDS
                    );
                    stopTimer = new StopOnlineTimer(scheduledFuture);
                    setFriends();
                    break;
                case OFFLINE:
                    if (reason != OnlineStatusReason.CONNECTION_ERROR)
                        setOffline();
                    if (scheduledTimer != null && !scheduledTimer.isShutdown())
                        scheduledTimer.submit(stopTimer);
                    longPollServer.stop();
                    friends = null;
                    break;
                case INVISIBLE:
                    longPollServer.start();
                    if (scheduledTimer != null && !scheduledTimer.isShutdown())
                        scheduledTimer.submit(stopTimer);
                    setOffline();
                    setFriends();
                    break;
            }
        } catch (RequestReturnErrorException | RequestReturnNullException e) {
            connectionError(e);
        }
        getBuddyChange().setState(onlineStatus);
        errorState = false;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setFriends() {
        if (friends != null)
            return;
        try {
            friends = new Friends().get(getUserId(), accessToken);
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            connectionError(e);
        }
    }

    public ArrayList<Buddy> getFriends() {
        return friends;
    }

    public LongPollServer getLongPollServer() {
        return longPollServer;
    }

    public void setLongPollServer(LongPollServer longPollServer) {
        this.longPollServer = longPollServer;
    }

    public Buddy getFriendById(ArrayList<Buddy> friends, int userId) {
        for (Buddy friend : friends) {
            if (friend.getUserId() == userId)
                return friend;
        }
        return null;
    }

    public boolean getAuthFinished() {
        return authFinished.get();
    }

    public void setAuthFinished(boolean authFinished) {
        this.authFinished.set(authFinished);
    }

    public BooleanProperty authFinishedProperty() {
        return authFinished;
    }

    private void setOnline() throws RequestReturnNullException, RequestReturnErrorException {
        JSONObject answer = networkHelper.sendRequest("account.setOnline",
                new WeakHashMap<String, String>() {{
                    put("access_token", getAccessToken());
                }}
        );
        if (answer.getInt("response") != 1)
            System.out.println("Online status error: " + answer.getInt("response"));
    }

    private void setOffline() throws RequestReturnNullException, RequestReturnErrorException {
        JSONObject answer = networkHelper.sendRequest("account.setOffline",
                new WeakHashMap<String, String>() {{
                    put("access_token", getAccessToken());
                }}
        );
        if (answer.getInt("response") != 1)
            System.out.println("Online status error: " + answer.getInt("response"));
        networkHelper.close();
    }

    /**
     * Send message to receiver  with userId
     *
     * @param userId  int user_id of the receiver
     * @param message Message
     * @return String message id
     * @throws RequestReturnNullException
     * @throws RequestReturnErrorException
     */
    public String sendMessage(final int userId, final Message message)
            throws RequestReturnNullException, RequestReturnErrorException {
        Map<String, String> urlParameters = new WeakHashMap<>();
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("access_token", getAccessToken());
        urlParameters.put("chat_id", "1");
        urlParameters.put("message", message.getBody());
        JSONObject answer = networkHelper.sendRequest("messages.send", urlParameters);
        return answer.toString();
    }

    /**
     * Get messages history
     *
     * @param userId receiver user_id
     * @param count  quantity of last messages
     * @param rev    reversion
     * @return ArrayList Messages
     */
    public ArrayList<Message> getMessagesHistory(final int userId, final int count, final int rev)
            throws RequestReturnNullException, RequestReturnErrorException {
        Map<String, String> urlParameters = new WeakHashMap<>();
        urlParameters.put("access_token", getAccessToken());
        urlParameters.put("count", Integer.toString(count));
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("rev", Integer.toString(rev));
        JSONObject obj = networkHelper.sendRequest("messages.getHistory", urlParameters).getJSONObject("response");
        return new MessageMapper().map(obj);
    }

    public ArrayList<Message> getMessagesById(final int messageId)
            throws RequestReturnNullException, RequestReturnErrorException {
        Map<String, String> urlParameters = new WeakHashMap<>();
        urlParameters.put("access_token", getAccessToken());
        urlParameters.put("message_ids", Integer.toString(messageId));
        JSONObject obj = networkHelper.sendRequest("messages.getById", urlParameters).getJSONObject("response");
        return new MessageMapper().map(obj);
    }

    /**
     * Show authorization window and create Account
     */
    public void getAuthHelper() {
        AuthHelper helper = new AuthHelper();
        helper.createAuthWindow();
        helper.isAnswerReceivedProperty().addListener(
                (ObservableValue<? extends Boolean> answer, Boolean oldAnswer, Boolean newAnswer) -> {
                    Map<String, String> list = helper.getAnswer();
                    try {
                        create(Integer.parseInt(list.get("user_id")), list.get("access_token"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setAuthFinished(true);
                });
    }

    public boolean isErrorState() {
        return errorState;
    }

    public void connectionError(Exception e) {
        errorState = true;
        setOnlineStatus(OnlineStatus.OFFLINE, OnlineStatusReason.CONNECTION_ERROR);
        System.out.println("Connection state error cause " + e.getMessage());
    }

    private final class StopOnlineTimer implements Runnable {
        private ScheduledFuture<?> scheduledFuture;

        StopOnlineTimer(ScheduledFuture<?> future) {
            scheduledFuture = future;
        }

        @Override
        public void run() {
            if (!scheduledFuture.isCancelled())
                scheduledFuture.cancel(true);
            scheduledTimer.shutdown();
        }
    }

}