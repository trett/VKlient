/*
 * (C) Copyright Tretyakov Roman.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package ru.trett.vkapi;

import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
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
    private String lpServer = null;
    private String lpServerKey = null;
    private String ts = null;
    private ScheduledExecutorService scheduledTimer;
    private Runnable stopTimer;
    private OnlineStatus onlineStatus;
    private LongPollServer longPollServer;

    public void create() {
        ArrayList<Buddy> buddies = VKUtils.getUsers(new ArrayList<Integer>() {{
            add(userId);
        }}, accessToken);
        if (buddies != null) {
            Buddy buddy = buddies.get(0);
            setFirstName(buddy.getFirstName());
            setLastName(buddy.getLastName());
            setStatus(buddy.getStatus());
            setAvatarURL(buddy.getAvatarURL());
            onlineStatusProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        System.out.println("Account change state to " + newValue.intValue());
                        if (newValue.intValue() == 1) {
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
        switch (onlineStatus) {
            case ONLINE:
                setOnlineStatusProperty(OnlineStatus.ONLINE.ordinal());
                scheduledTimer = Executors.newSingleThreadScheduledExecutor();
                ScheduledFuture<?> scheduledFuture = scheduledTimer.scheduleAtFixedRate(
                        () -> VKUtils.setOnline(Account.this.getAccessToken()), 5, 90, TimeUnit.SECONDS
                );
                stopTimer = new StopOnlineTimer(scheduledFuture);
                if (friends != null)
                    break;
                setFriends();
                break;
            case OFFLINE:
                setOnlineStatusProperty(OnlineStatus.OFFLINE.ordinal());
                VKUtils.setOffline(getAccessToken());
                if (scheduledTimer != null)
                    scheduledTimer.submit(stopTimer);
                VKUtils.abortAllConnections();
                break;
            case INVISIBLE:
                setOnlineStatusProperty(OnlineStatus.ONLINE.ordinal());
                if (scheduledTimer != null)
                    scheduledTimer.submit(stopTimer);
                VKUtils.setOffline(getAccessToken());
                break;
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setFriends() {
        friends = VKUtils.getFriends(userId, accessToken);
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

    //    private void getLongPollConnection() {
//        HashMap<String, String> longPollServer = VKUtils.getLongPollServer(Account.this.getAccessToken());
//        if (longPollServer == null)
//            throw new RuntimeException("Can't get long poll server.");
//        lpServer = longPollServer.get("server");
//        lpServerKey = longPollServer.get("key");
//        ts = longPollServer.get("ts");
//    }
//
//    private void longPollConnection() {
//        getLongPollConnection();
//        Timer timer = new Timer();
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                while (onlineStatus != OnlineStatus.OFFLINE) {
//                    try {
//                        String answer = VKUtils.getUpdates(lpServer, lpServerKey, ts);
//                        assert answer != null;
//                        JSONObject json = new JSONObject(answer);
//                        if (json.has("failed")) {
//                            getLongPollConnection();
//                        } else {
//                            ts = json.optString("ts");
//                            JSONArray array = json.getJSONArray("updates");
//                            UpdatesHandler.update(array, Account.this);
//                        }
//                    } catch (RequestReturnNullException e) {
//                        System.out.println(e.getMessage());
//                    }
//                }
//            }
//        };
//        timer.schedule(timerTask, 5000);
//    }

    public Buddy getFriendById(ArrayList<Buddy> friends, int userId) {
        for (Buddy friend : friends) {
            if (friend.getUserId() == userId)
                return friend;
        }
        return null;
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