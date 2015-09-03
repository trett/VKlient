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

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vkauth.Buddy;
import ru.trett.vkauth.BuddyImpl;
import ru.trett.vkauth.Message;
import ru.trett.vkauth.VKUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Account extends BuddyImpl {

    private int userId = 0;
    private String accessToken = null;
    private ArrayList<BuddyImpl> friends = null;
    private String lpServer = null;
    private String lpServerKey = null;
    private String ts = null;
    private Timer onlineTimer = new Timer();
    private BooleanProperty tokenExpire = new SimpleBooleanProperty(true);

    public Account() {
        Config config = new Config();
        userId = Integer.parseInt(config.getValue("user_id"));
        accessToken = config.getValue("access_token");
        Map<String, String> name = VKUtils.getBuddy(userId, accessToken);
        if (name != null) {
            setFirstName(name.get("firstName"));
            setLastName(name.get("lastName"));
            setStatus(name.get("status"));
            setAvatarURL(name.get("avatarURL"));
            tokenExpireProperty().addListener(
                    (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        if (newValue)
                            System.out.println("Token is expire");
                    });
            onlineStatusProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        System.out.println("Account change state to " + newValue.intValue());
                        if (newValue.intValue() == 1)
                            longPollConnection();
                    });
            setOnlineStatus(VKUtils.OnlineStatus.ONLINE);
        } else {
            setTokenExpire(false);
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
    public void setOnlineStatus(int online) {
        if (online == 1) {
            setOnlineStatusProperty(1);
            setFriends();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    VKUtils.setOnline(Account.this);
                }
            };
            onlineTimer.schedule(timerTask, 5000, 900000);
        } else {
            setOnlineStatusProperty(0);
            onlineTimer.cancel();
            VKUtils.abortAllConnections();
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean getTokenExpire() {
        return tokenExpire.get();
    }

    public void setTokenExpire(boolean tokenExpire) {
        this.tokenExpire.set(tokenExpire);
    }

    public BooleanProperty tokenExpireProperty() {
        return tokenExpire;
    }

    public void setFriends() {
        friends = VKUtils.getFriends(userId, accessToken);
    }

    public ArrayList<BuddyImpl> getFriends() {
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

    public void longPollConnection() {
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
                            update(array);
                        }
                    } else {
                        getLongPollConnection();
                    }
                }
            }
        };
        timer.schedule(timerTask, 5000);
    }

    public Buddy getFriendById(ArrayList<BuddyImpl> friends, int userId) {
        for (int i = 0; i < friends.size(); ++i) {
            if (friends.get(i).getUserId() == userId)
                return friends.get(i);
        }
        return null;
    }

    private void update(JSONArray array) {
        for (int i = 0; i < array.length(); ++i) {
            JSONArray temp = array.getJSONArray(i);
            ArrayList<Object> list = new ArrayList<>();
            for (int j = 0; j < temp.length(); ++j) {
                list.add(temp.get(j));
            }
            //TODO: parse all !!!
            switch ((int) list.get(0)) {
                case 8:
                    getFriendById(friends, -(int) list.get(1)).setOnlineStatus(1); //TODO:parse to platform
                    break;
                case 9:
                    getFriendById(friends, -(int) list.get(1)).setOnlineStatus(0);
                    break;
                case 4:
                    Platform.runLater(() -> {
                        int flag = (int) list.get(2);

                        Message message = new Message();
                        Date date = new Date(Long.parseLong(list.get(4).toString()) * 1000);
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        message.setDate(sdf.format(date));
                        message.setBody(list.get(6).toString());
                        message.setDirection(
                                (flag & VKUtils.MessageFlags.OUTBOX) == VKUtils.MessageFlags.OUTBOX ?
                                        "out" : "in"
                        );
                        ChatWindow chatWindow = ChatWindowFactory.getInstance(this, (int) list.get(3));
                        if (chatWindow != null && chatWindow.isShowing()) {
                            chatWindow.appendMessage(message);
                        } else if (chatWindow != null) {
                            Buddy b = getFriendById(friends, (int) list.get(3));
                            b.setNewMessages(1);
                            chatWindow.appendMessage(message);
                            NotificationProvider.showNotification(b.getFirstName() + " " + b.getLastName());
                        } else {
                            Buddy b = getFriendById(friends, (int) list.get(3));
                            b.setNewMessages(1);
                            NotificationProvider.showNotification(b.getFirstName() + " " + b.getLastName());
                        }
                    });
                    break;

                default:
                    break;
            }
        }
    }

}