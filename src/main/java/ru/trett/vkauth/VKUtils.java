package ru.trett.vkauth;

import com.vdurmont.emoji.EmojiParser;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vklient.Account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class VKUtils {

    public static Map<String, String> getBuddy(int userId, String token) {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("access_token", token);
        urlParameters.put("fields", "photo_50,online,status");
        Map<String, String> name = new HashMap<>();
        JSONObject obj = requestBuilder("users.get", urlParameters);
        if (obj == null || obj.has("error"))
            return null;
        JSONArray json = obj.getJSONArray("response");
        name.put("firstName", json.getJSONObject(0).getString("first_name"));
        name.put("lastName", json.getJSONObject(0).getString("last_name"));
        name.put("avatarURL", json.getJSONObject(0).getString("photo_50"));
        name.put("onlineStatus", Integer.toString(json.getJSONObject(0).getInt("online")));
        name.put("status", json.getJSONObject(0).getString("status"));
        return name;
    }

    public static ArrayList<BuddyImpl> getFriends(int userId, String token) {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("access_token", token);
        urlParameters.put("fields", "first_name,last_name,photo_50,online,status");
        JSONObject obj = requestBuilder("friends.get", urlParameters);
        if (obj == null)
            return null;
        JSONArray json = obj.getJSONArray("response");
        ArrayList<BuddyImpl> buddies = new ArrayList<>();
        for (int i = 0; i < json.length(); ++i) {
            BuddyImpl buddy = new BuddyImpl();
            buddy.setUserId(json.getJSONObject(i).getInt("user_id"));
            buddy.setFirstName(json.getJSONObject(i).getString("first_name"));
            buddy.setLastName(json.getJSONObject(i).getString("last_name"));
            buddy.setAvatarURL(json.getJSONObject(i).getString("photo_50"));
            buddy.setOnlineStatus(json.getJSONObject(i).getInt("online"));
            buddy.setStatus(json.getJSONObject(i).getString("status"));
            buddies.add(buddy);
        }
        return buddies;
    }

    public static String getMessagesHistory(Account account, int userId, int count, int rev) {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", account.getAccessToken());
        urlParameters.put("count", Integer.toString(count));
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("rev", Integer.toString(rev));
        JSONObject obj = requestBuilder("messages.getHistory", urlParameters);
        if (obj == null)
            return null;
        JSONArray array = obj.getJSONArray("response");
        StringBuilder content = new StringBuilder();
        StringBuilder message = new StringBuilder();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        for (int i = 1; i < array.length(); ++i) {
            date.setTime(array.getJSONObject(i).getLong("date") * 1000);
            if (array.getJSONObject(i).getInt("out") == 0) {
                message.append("<div id='incomingMessage'>[");
                message.append(sdf.format(date));
                message.append("] ");
            } else {
                message.append("<div id='outcomingMessage'>[");
                message.append(sdf.format(date));
                message.append("] ");
            }
            if (array.getJSONObject(i).has("emoji")) {
                message.append(EmojiParser.parseToHtmlDecimal(array.getJSONObject(i).getString("body")));

            } else {
                message.append(array.getJSONObject(i).getString("body"));
            }
            message.append("</div>");
            content.insert(0, message.toString());
            message.setLength(0);
        }
        return content.toString();
    }

    public static HashMap<String, String> getLongPollServer(Account account) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("access_token", account.getAccessToken());
            String url = Request.send("api.vk.com/method/", "messages.getLongPollServer", urlParameters);
            System.out.println("Get Server:" + url);
            if (url == null)
                return null;
            JSONObject obj = new JSONObject(url);
            HashMap<String, String> lpServerMap = new HashMap<>();
            lpServerMap.put("server", obj.getJSONObject("response").getString("server"));
            lpServerMap.put("key", obj.getJSONObject("response").getString("key"));
            lpServerMap.put("ts", Integer.toString(obj.getJSONObject("response").getInt("ts")));
            return lpServerMap;
        } catch (ClientProtocolException e) {
            return null;
        }
    }

    public static String getUpdates(String server, String key, String ts) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("act", "a_check");
            urlParameters.put("key", key);
            urlParameters.put("ts", ts);
            urlParameters.put("wait", "25");
            urlParameters.put("mode", "2");
            return Request.send(server, "", urlParameters);
        } catch (ClientProtocolException e) {
            return null;
        }
    }

    public static String sendMessage(Account account, int userId, String message) {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("access_token", account.getAccessToken());
        urlParameters.put("chat_id", "1");
        urlParameters.put("message", message);
        JSONObject answer = requestBuilder("messages.send", urlParameters);
        if (answer == null)
            return null;
        return answer.toString();
    }

    public static void setOnline(Account account) {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", account.getAccessToken());
        JSONObject answer = requestBuilder("account.setOnline", urlParameters);
        if (answer == null && answer.getInt("response") != 1)
            System.out.println("Online status error: " + answer.getInt("response"));
    }

    public static boolean checkToken(String token) {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", token);
        JSONObject obj = requestBuilder("users.get", urlParameters);
        if (obj == null || obj.has("error"))
            return false;
        return true;
    }

    private static JSONObject requestBuilder(String vkMethod, HashMap<String, String> urlParameters) {
        try {
            String str = Request.send("api.vk.com/method/", vkMethod, urlParameters);
            if (str == null)
                return null;
            JSONObject receivedAnswer = new JSONObject(str);
            System.out.println(urlParameters); //debug output
            return receivedAnswer;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class MessageFlags {

        public static final int UNREAD = 1;
        public static final int OUTBOX = 2;
        public static final int REPLIED = 4;
        public static final int IMPORTANT = 8;
        public static final int CHAT = 16;
        public static final int FRIENDS = 32;
        public static final int SPAM = 64;
        public static final int DELЕTЕD = 128;
        public static final int FIXED = 256;
        public static final int MEDIA = 512;

    }

    public static class OnlineStatus  {
        public static final int OFFLINE = 0;
        public static final int ONLINE = 1;

    }

}
