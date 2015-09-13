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

package ru.trett.vklient;

import javafx.beans.value.ObservableValue;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vkauth.Buddy;
import ru.trett.vkauth.BuddyImpl;
import ru.trett.vkauth.OnlineStatus;
import ru.trett.vkauth.VKUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
    private Timer onlineTimer = new Timer();

    public Account() {
        Config config = new Config();
        userId = Integer.parseInt(config.getValue("user_id"));
        accessToken = config.getValue("access_token");
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
                        if (newValue.intValue() == 1)
                            longPollConnection();
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
    public void setOnlineStatus(OnlineStatus onlineStatus) {
        switch (onlineStatus) {
            case ONLINE:
                setOnlineStatusProperty(OnlineStatus.ONLINE.ordinal());
                setFriends();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        VKUtils.setOnline(Account.this);
                    }
                };
                onlineTimer.schedule(timerTask, 5000, 900000);
                break;
            case OFFLINE:
                setOnlineStatusProperty(OnlineStatus.OFFLINE.ordinal());
                VKUtils.setOffline(this);
                onlineTimer.cancel();
                VKUtils.abortAllConnections();
                break;
            case INVISIBLE:
                setOnlineStatusProperty(OnlineStatus.ONLINE.ordinal());
                onlineTimer.cancel();
                VKUtils.setOffline(this);
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

    private void getLongPollConnection() {
        HashMap<String, String> longPollServer = VKUtils.getLongPollServer(Account.this);
        if (longPollServer == null)
            throw new RuntimeException("Can't get long poll server.");
        lpServer = longPollServer.get("server");
        lpServerKey = longPollServer.get("key");
        ts = longPollServer.get("ts");
    }

    private void longPollConnection() {
        getLongPollConnection();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                while (getOnlineStatusProperty() == 1) {
                    String answer = VKUtils.getUpdates(lpServer, lpServerKey, ts);
                    if (answer != null) {
                        JSONObject json = new JSONObject(answer);
                        if (json.has("failed")) {
                            getLongPollConnection();
                        } else {
                            ts = json.optString("ts");
                            JSONArray array = json.getJSONArray("updates");
                            UpdatesHandler.update(array, Account.this);
                        }
                    } else {
                        getLongPollConnection();
                    }
                }
            }
        };
        timer.schedule(timerTask, 5000);
    }

    public Buddy getFriendById(ArrayList<Buddy> friends, int userId) {
        for (Buddy friend : friends) {
            if (friend.getUserId() == userId)
                return friend;
        }
        return null;
    }

}