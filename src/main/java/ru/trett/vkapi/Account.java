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

import javafx.beans.value.ObservableValue;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Account extends BuddyImpl {

    private int userId = 0;
    private String accessToken = null;
    private ArrayList<Buddy> friends = null;
    private ScheduledExecutorService scheduledTimer;
    private Runnable stopTimer;
    private OnlineStatus onlineStatus;
    private LongPollServer longPollServer;

    public void create() {
        ArrayList<Buddy> buddies = null;
        try {
            buddies = Users.get(new ArrayList<Integer>() {{
                add(userId);
            }}, accessToken);
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
        }
        if (buddies != null) {
            Buddy buddy = buddies.get(0);
            setFirstName(buddy.getFirstName());
            setLastName(buddy.getLastName());
            setStatus(buddy.getStatus());
            setAvatarURL(buddy.getAvatarURL());
            onlineStatusProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldStatus, Number newStatus) -> {
                        System.out.println("Account change state to " + newStatus.intValue());
                        if (newStatus.intValue() == 1) {
                            longPollServer = new LongPollServer(this);
                        }
                    });
        } else {
            throw new RuntimeException("Impossible to create Account");
        }
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public OnlineStatus getOnlineStatus() {
        return this.onlineStatus;
    }

    @Override
    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.onlineStatus = onlineStatus;
        try {
            switch (onlineStatus) {
                case ONLINE:
                    setOnlineStatusProperty(OnlineStatus.ONLINE.ordinal());
                    scheduledTimer = Executors.newSingleThreadScheduledExecutor();
                    ScheduledFuture<?> scheduledFuture = scheduledTimer.scheduleAtFixedRate(
                            () -> {
                                try {
                                    setOnline();
                                } catch (RequestReturnNullException | RequestReturnErrorException e) {
                                    e.printStackTrace();
                                }
                            }, 5, 90, TimeUnit.SECONDS);
                    stopTimer = new StopOnlineTimer(scheduledFuture);
                    if (friends != null)
                        break;
                    setFriends();
                    break;
                case OFFLINE:
                    setOnlineStatusProperty(OnlineStatus.OFFLINE.ordinal());
                    setOffline();
                    if (scheduledTimer != null && !scheduledTimer.isShutdown())
                        scheduledTimer.submit(stopTimer);
                    if (longPollServer != null)
                        longPollServer.close();
                    NetworkHelper.close();
                    break;
                case INVISIBLE:
                    setOnlineStatusProperty(OnlineStatus.ONLINE.ordinal());
                    if (scheduledTimer != null && !scheduledTimer.isShutdown())
                        scheduledTimer.submit(stopTimer);
                    setOffline();
                    break;
            }
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setFriends() {
        try {
            friends = Friends.get(userId, accessToken);
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
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

    public void setOnline()
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", getAccessToken());
        JSONObject answer = NetworkHelper.sendRequest("account.setOnline", urlParameters);
        if (answer.getInt("response") != 1)
            System.out.println("Online status error: " + answer.getInt("response"));
    }

    public void setOffline()
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", getAccessToken());
        JSONObject answer = NetworkHelper.sendRequest("account.setOffline", urlParameters);
        if (answer.getInt("response") != 1)
            System.out.println("Online status error: " + answer.getInt("response"));
    }

    public String sendMessage(int userId, Message message)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("access_token", getAccessToken());
        urlParameters.put("chat_id", "1");
        urlParameters.put("message", message.getBody());
        JSONObject answer = NetworkHelper.sendRequest("messages.send", urlParameters);
        return answer.toString();
    }

    /**
     * Get messages history
     *
     * @param userId
     * @param count
     * @param rev
     * @return ArrayList Messages
     */
    public ArrayList<Message> getMessagesHistory(int userId, int count, int rev)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", getAccessToken());
        urlParameters.put("count", Integer.toString(count));
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("rev", Integer.toString(rev));
        JSONObject obj = NetworkHelper.sendRequest("messages.getHistory", urlParameters).getJSONObject("response");
        return new MessageMapper().map(obj);
    }

    public ArrayList<Message> getMessagesById(int messageId)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", getAccessToken());
        urlParameters.put("message_ids", Integer.toString(messageId));
        JSONObject obj = NetworkHelper.sendRequest("messages.getById", urlParameters).getJSONObject("response");
        return new MessageMapper().map(obj);
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