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

package ru.trett.vkauth;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vklient.Account;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class VKUtils {

    private static NetworkClient networkClient;
    private static NetworkClient longPollClient;
    private static final double API_VERSION = 5.37;

    static {
        networkClient = new NetworkClient(5000);
        longPollClient = new NetworkClient(26000);
    }

    public static Map<String, String> getBuddy(int userId, String token) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("user_id", Integer.toString(userId));
            urlParameters.put("access_token", token);
            urlParameters.put("fields", "photo_50,online,status");
            Map<String, String> name = new HashMap<>();
            JSONObject obj = requestBuilder("users.get", urlParameters);
            JSONArray json = obj.getJSONArray("response");
            name.put("firstName", json.getJSONObject(0).getString("first_name"));
            name.put("lastName", json.getJSONObject(0).getString("last_name"));
            name.put("avatarURL", json.getJSONObject(0).getString("photo_50"));
            name.put("onlineStatus", Integer.toString(json.getJSONObject(0).getInt("online")));
            name.put("status", json.getJSONObject(0).getString("status"));
            return name;
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<BuddyImpl> getFriends(int userId, String token) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("user_id", Integer.toString(userId));
            urlParameters.put("access_token", token);
            urlParameters.put("fields", "first_name,last_name,photo_50,online,status");
            JSONObject obj = requestBuilder("friends.get", urlParameters).getJSONObject("response");
            JSONArray json = obj.getJSONArray("items");
            ArrayList<BuddyImpl> buddies = new ArrayList<>();
            for (int i = 0; i < json.length(); ++i) {
                BuddyImpl buddy = new BuddyImpl();
                buddy.setUserId(json.getJSONObject(i).getInt("id"));
                buddy.setFirstName(json.getJSONObject(i).getString("first_name"));
                buddy.setLastName(json.getJSONObject(i).getString("last_name"));
                buddy.setAvatarURL(json.getJSONObject(i).getString("photo_50"));
                buddy.setOnlineStatus(json.getJSONObject(i).getInt("online"));
                buddy.setStatus(json.getJSONObject(i).getString("status"));
                buddies.add(buddy);
            }
            return buddies;
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Message> getMessagesHistory(Account account, int userId, int count, int rev) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("access_token", account.getAccessToken());
            urlParameters.put("count", Integer.toString(count));
            urlParameters.put("user_id", Integer.toString(userId));
            urlParameters.put("rev", Integer.toString(rev));
            JSONObject obj = requestBuilder("messages.getHistory", urlParameters).getJSONObject("response");
            JSONArray array = obj.getJSONArray("items");
            ArrayList<Message> messages = new ArrayList<>();
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            for (int i = 0; i < array.length(); ++i) {
                Message m = new Message();
                date.setTime(array.getJSONObject(i).getLong("date") * 1000);
                if (array.getJSONObject(i).getInt("out") == 1) {
                    m.setDirection("out");
                } else {
                    m.setDirection("in");
                }
                m.setBody(array.getJSONObject(i).getString("body"));
                m.setDate(sdf.format(date));
                if (array.getJSONObject(i).has("attachments")) {
                    JSONArray attachments = array.getJSONObject(i).getJSONArray("attachments");
                    for (int j = 0; j < attachments.length(); ++j)
                        m.addAttachment(attachments.getJSONObject(j), false);
                }
                messages.add(m);
            }
            Collections.reverse(messages);
            return messages;
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> getLongPollServer(Account account) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("access_token", account.getAccessToken());
            urlParameters.put("v", Double.toString(API_VERSION));
            Request request = new RequestBuilder().
                    host("api.vk.com/method/").
                    path("messages.getLongPollServer").
                    query(urlParameters).
                    build();
            String url = networkClient.send(request);
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
            Request request = new RequestBuilder().
                    host(server).
                    query(urlParameters).
                    build();
            return longPollClient.send(request);
        } catch (ClientProtocolException e) {
            return null;
        }
    }

    public static String sendMessage(Account account, int userId, Message message) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("user_id", Integer.toString(userId));
            urlParameters.put("access_token", account.getAccessToken());
            urlParameters.put("chat_id", "1");
            urlParameters.put("message", message.getBody());
            JSONObject answer = requestBuilder("messages.send", urlParameters);
            return answer.toString();
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setOnline(Account account) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("access_token", account.getAccessToken());
            JSONObject answer = requestBuilder("account.setOnline", urlParameters);
            if (answer.getInt("response") != 1)
                System.out.println("Online status error: " + answer.getInt("response"));
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkToken(String token) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("access_token", token);
            JSONObject obj = requestBuilder("users.get", urlParameters);
            return true;
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static JSONObject requestBuilder(String vkMethod, HashMap<String, String> urlParameters)
            throws RequestReturnNullException, RequestReturnErrorException {
        try {
            urlParameters.put("v", Double.toString(API_VERSION));
            Request request = new RequestBuilder().host("api.vk.com/method/").
                    path(vkMethod).query(urlParameters).build();
            String str = networkClient.send(request);
            if (str == null)
                throw new RequestReturnNullException("NetworkClient return null");
            JSONObject receivedAnswer = new JSONObject(str);
            if (receivedAnswer.has("error")) {
                throw new RequestReturnErrorException(
                        "NetworkClient return error: " + receivedAnswer.getJSONObject("error").toString());
            }
            System.out.println(urlParameters); //debug output
            return receivedAnswer;
        } catch (ClientProtocolException e) {
            throw new RequestReturnNullException("NetworkClient return null.", e);
        }
    }

    public static void abortAllConnections() {
        networkClient.abort();
        longPollClient.abort();
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

    public static class OnlineStatus {
        public static final int OFFLINE = 0;
        public static final int ONLINE = 1;

    }

}
