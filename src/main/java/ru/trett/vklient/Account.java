package ru.trett.vklient;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vkauth.AuthHelper;
import ru.trett.vkauth.BuddyImpl;
import ru.trett.vkauth.VKUtils;

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
        update();
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

    public void update() {
        HashMap<String, String> longPollServer = VKUtils.getLongPollServer(Account.this);
        lpServer = longPollServer.get("server");
        lpServerKey = longPollServer.get("key");
        ts = longPollServer.get("ts");

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    String updates = VKUtils.getUpdates(lpServer, lpServerKey, ts);
                    if (updates != null) {
                        JSONObject json = new JSONObject(updates);
                        ts = json.optString("ts");
                        JSONArray array = json.getJSONArray("updates");
                        System.out.println(updates);
//                        TODO:// make a getBuddyById and update by this info
                    }
                }
            }
        };
        timer.schedule(timerTask, 10000);
    }

}