package ru.trett.vklient;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import com.vdurmont.emoji.EmojiParser;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vkauth.Buddy;
import ru.trett.vkauth.BuddyImpl;
import ru.trett.vkauth.VKUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class Account extends BuddyImpl {

    private int userId = 0;
    private String accessToken = null;
    private ArrayList<BuddyImpl> friends = null;
    private String lpServer = null;
    private String lpServerKey = null;
    private String ts = null;

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
            setFriends();
            onlineStatusProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        System.out.println("Account change state to " + newValue.intValue());
                        if (newValue.intValue() == 1)
                            longPollConnection();
                        else
                            setOnlineStatus(0);
                    });
            setOnlineStatus(1);
        } else {
            throw new RuntimeException();
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
        Timer timer = new Timer();
        if (online == 1) {
            setOnlineStatusProperty(1);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    VKUtils.setOnline(Account.this);
                }
            };
            timer.schedule(timerTask, 5000, 900000);
        } else {
            setOnlineStatusProperty(0);
            timer.cancel();
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

    public ArrayList<BuddyImpl> getFriends() {
        return friends;
    }

    private void getLongPollConnection() {
        HashMap<String, String> longPollServer = VKUtils.getLongPollServer(Account.this);
        assert (longPollServer == null);
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
                        if ((flag & VKUtils.MessageFlags.OUTBOX) != VKUtils.MessageFlags.OUTBOX) {
                            Date date = new Date((int) list.get(4) * 1000);
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            StringBuilder message = new StringBuilder(
                                    EmojiParser.parseToHtmlDecimal(list.get(6).toString())
                            );
                            message.insert(0, "[" + sdf.format(date) + "] ");
                            ChatWindowFactory.getNewInstance(this, (int) list.get(3)).
                                    appendMessage(message.toString(), true);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

}