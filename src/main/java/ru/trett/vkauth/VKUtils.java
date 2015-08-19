package ru.trett.vkauth;

import com.vdurmont.emoji.EmojiParser;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vklient.Account;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
        try {
            String urlParameters = "user_id=" + URLEncoder.encode(Integer.toString(userId), "UTF-8") +
                    "&access_token=" + URLEncoder.encode(token, "UTF-8") + "&fields=photo_50,online,status";
            Map<String, String> name = new HashMap<>();
            JSONObject obj = requestBuilder("users.get", urlParameters);
            JSONArray json = obj.getJSONArray("response");
            name.put("firstName", json.getJSONObject(0).getString("first_name"));
            name.put("lastName", json.getJSONObject(0).getString("last_name"));
            name.put("avatarURL", json.getJSONObject(0).getString("photo_50"));
            name.put("onlineStatus", Integer.toString(json.getJSONObject(0).getInt("online")));
            name.put("status", json.getJSONObject(0).getString("status"));
            return name;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<BuddyImpl> getFriends(int userId, String token) {
        try {
            String urlParameters = "user_id=" + URLEncoder.encode(Integer.toString(userId), "UTF-8") +
                    "&access_token=" + URLEncoder.encode(token, "UTF-8") + "&fields=first_name,last_name,photo_50,online,status";
            JSONObject obj = requestBuilder("friends.get", urlParameters);
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
        } catch (UnsupportedEncodingException|NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMessagesHistory(Account account, int userId, int count, int rev) {
        try {
            String urlParameters = "access_token=" + URLEncoder.encode(account.getAccessToken(), "UTF-8") +
                    "&count=" + count +
                    "&user_id=" + URLEncoder.encode(Integer.toString(userId), "UTF-8") +
                    "&rev=" + rev;
            JSONObject obj = requestBuilder("messages.getHistory", urlParameters);
            JSONArray array = obj.getJSONArray("response");
            StringBuilder content = new StringBuilder();
            StringBuilder message = new StringBuilder();
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.");
            for (int i = 1; i < array.length(); ++i) {
                date.setTime(array.getJSONObject(i).getLong("date") * 1000);//.toString();
                message.append("<p>[" + sdf.format(date) + "] ");
                if (array.getJSONObject(i).has("emoji")) {
                    message.append(EmojiParser.parseToHtmlDecimal(array.getJSONObject(i).getString("body")) + "<br />");

                } else {
                    message.append(array.getJSONObject(i).getString("body") + "<br />");
                }
                message.append("</p>");
                content.insert(0, message.toString());
                message.setLength(0);
            }
            content.insert(0, "<head><style>p { font: 10pt sans-serif; }</style></head>"); //temporary
            return content.toString();
        } catch (UnsupportedEncodingException|NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateAccountInfo(Account account) {
        try {
            String urlParameters = "user_id=" + URLEncoder.encode(Integer.toString(account.getUserId()), "UTF-8") +
                    "&access_token=" + URLEncoder.encode(account.getAccessToken(), "UTF-8") + "&fields=first_name,last_name,online,status";
            JSONObject obj = requestBuilder("friends.get", urlParameters);
            JSONArray json = obj.getJSONArray("response");
            ArrayList<BuddyImpl> friends = account.getFriends();
            for (int i = 0; i < json.length(); ++i) {
                friends.get(i).setUserId(json.getJSONObject(i).getInt("user_id"));
                friends.get(i).setFirstName(json.getJSONObject(i).getString("first_name"));
                friends.get(i).setLastName(json.getJSONObject(i).getString("last_name"));
//                    friends.get(i).setAvatarURL(json.getJSONObject(i).getString("photo_50"));
                friends.get(i).setOnlineStatus(json.getJSONObject(i).getInt("online"));
                friends.get(i).setStatus(json.getJSONObject(i).getString("status"));
            }
        } catch (UnsupportedEncodingException|NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject requestBuilder(String vkMethod, String urlParameters) throws NullPointerException{
        StringBuilder url = new StringBuilder("https://api.vk.com/method/");
        url.append(vkMethod);
        String str = Request.sendRequest(url.toString(), urlParameters);
        if (str == null)
            throw new NullPointerException();
        JSONObject recievedAnswer = new JSONObject(str);
        System.out.println(urlParameters + System.getProperty("line.separator") + recievedAnswer); //debug output
        return recievedAnswer;
    }

}
