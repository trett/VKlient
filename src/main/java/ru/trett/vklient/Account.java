package ru.trett.vklient;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import com.vdurmont.emoji.EmojiParser;
import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vkauth.AuthHelper;
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
        AuthHelper helper = new AuthHelper();
        Map<String, String> userData = helper.getToken();
        userId = Integer.parseInt(userData.get("user_id"));
        accessToken = userData.get("access_token");
        Map<String, String> name = VKUtils.getBuddy(userId, accessToken);
        setFirstName(name.get("firstName"));
        setLastName(name.get("lastName"));
        setOnlineStatus(Integer.parseInt(name.get("onlineStatus")));
        setStatus(name.get("status"));
        setAvatarURL(name.get("avatarURL"));
        setFriends();
//        updateInfo();
        longPollConnection();
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
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

//    public void updateInfo() {
//        Timer timer = new Timer();
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("-----------------------Updating -----------------------------------------");
//                VKUtils.updateAccountInfo(Account.this);
//                System.out.println("-----------------------Updated-------------------------------------------");
//            }
//        };
//
//        timer.schedule(timerTask, 10000, 60000);
//    }

    private void getLongPollConnection() {
        HashMap<String, String> longPollServer = VKUtils.getLongPollServer(Account.this);
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
                while (true) {
                    String answer = VKUtils.getUpdates(lpServer, lpServerKey, ts);
                    if (answer != null) {
                        JSONObject json = new JSONObject(answer);
                        if (json.has("failed")) {
                            getLongPollConnection();
                        } else {
                            ts = json.optString("ts");
                            JSONArray array = json.getJSONArray("updates");
                            System.out.println(answer);
                            update(array);
                        }
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
                        //TODO: parse flag answer
                        if (flag == 49 || flag == 33 || flag == 51) {
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