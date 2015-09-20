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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class VKUtils {

    private static final double API_VERSION = 5.37;
    private static NetworkClient networkClient;
    private static NetworkClient longPollClient;

    static {
        networkClient = new NetworkClient(5000);
        longPollClient = new NetworkClient(26000);
    }

    /**
     * Get users by id
     *
     * @param userIds List of Integer ids
     * @param token   String
     * @return ArrayList buddies
     */
    public static ArrayList<Buddy> getUsers(List<Integer> userIds, String token)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        String ids = userIds.stream().map(x -> x.toString()).collect(Collectors.joining(","));
        urlParameters.put("user_ids", ids);
        urlParameters.put("access_token", token);
        urlParameters.put("fields", "photo_50,online,status");
        JSONObject obj = sendRequest("users.get", urlParameters);
        return userMapper(obj.getJSONArray("response"));
    }

    /**
     * Get friends for given user_id
     *
     * @param userId
     * @param token
     * @return ArrayList buddies
     */
    public static ArrayList<Buddy> getFriends(int userId, String token)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("access_token", token);
        urlParameters.put("fields", "first_name,last_name,photo_50,online,status");
        JSONObject obj = sendRequest("friends.get", urlParameters).getJSONObject("response");
        return userMapper(obj.getJSONArray("items"));
    }

    /**
     * Get messages history
     *
     * @param token  Access token
     * @param userId
     * @param count
     * @param rev
     * @return ArrayList Messages
     */
    public static ArrayList<Message> getMessagesHistory(String token, int userId, int count, int rev)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", token);
        urlParameters.put("count", Integer.toString(count));
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("rev", Integer.toString(rev));
        JSONObject obj = sendRequest("messages.getHistory", urlParameters).getJSONObject("response");
        return answerToMessages(obj);
    }

    public static HashMap<String, String> getLongPollServer(String token)
            throws RequestReturnNullException {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("access_token", token);
            urlParameters.put("v", Double.toString(API_VERSION));
            Request request = new RequestBuilder().
                    host("api.vk.com/method/").
                    path("messages.getLongPollServer").
                    query(urlParameters).
                    build();
            String url = networkClient.send(request);
            if (url == null)
                throw new RequestReturnNullException("Can't get long poll server url.");
            System.out.println("Get Server:" + url);
            JSONObject obj = new JSONObject(url);
            HashMap<String, String> lpServerMap = new HashMap<>();
            lpServerMap.put("server", obj.getJSONObject("response").getString("server"));
            lpServerMap.put("key", obj.getJSONObject("response").getString("key"));
            lpServerMap.put("ts", Integer.toString(obj.getJSONObject("response").getInt("ts")));
            return lpServerMap;
        } catch (ClientProtocolException e) {
            throw new RequestReturnNullException("Can't get Long Poll server cause: ", e);
        }
    }

    public static JSONObject getUpdates(String server, String key, String ts)
            throws RequestReturnNullException, RequestReturnErrorException {
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
            String answer = longPollClient.send(request);
            if (answer == null)
                throw new RequestReturnNullException("Long Poll server return null");
            JSONObject obj = new JSONObject(answer);
            if (obj.has("error"))
                throw new RequestReturnErrorException("NetworkClient return error: "
                        + obj.getJSONObject("error").toString());
            return obj;
        } catch (ClientProtocolException e) {
            throw new RequestReturnNullException("Can't get Long Poll server cause: ", e);
        }
    }

    public static String sendMessage(String token, int userId, Message message)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("access_token", token);
        urlParameters.put("chat_id", "1");
        urlParameters.put("message", message.getBody());
        JSONObject answer = sendRequest("messages.send", urlParameters);
        return answer.toString();
    }

    public static void setOnline(String token)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", token);
        JSONObject answer = sendRequest("account.setOnline", urlParameters);
        if (answer.getInt("response") != 1)
            System.out.println("Online status error: " + answer.getInt("response"));
    }

    public static void setOffline(String token)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", token);
        JSONObject answer = sendRequest("account.setOffline", urlParameters);
        if (answer.getInt("response") != 1)
            System.out.println("Online status error: " + answer.getInt("response"));
    }

    /**
     * Return user_id for given token
     *
     * @param token
     * @return int user_id or 0 if token wrong
     */
    public static int getUsers(String token) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("access_token", token);
            JSONObject json = sendRequest("users.get", urlParameters);
            return json.getJSONArray("response").getJSONObject(0).getInt("id");
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static ArrayList<Message> getMessagesById(String token, int messageId)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("access_token", token);
        urlParameters.put("message_ids", Integer.toString(messageId));
        JSONObject obj = sendRequest("messages.getById", urlParameters).getJSONObject("response");
        return answerToMessages(obj);
    }

    private static JSONObject sendRequest(String vkMethod, HashMap<String, String> urlParameters)
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

    public static ArrayList<Message> answerToMessages(JSONObject object) {
        if(object == null || !object.has("items"))
            return null;
        JSONArray array = object.getJSONArray("items");
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
                    m.addAttachment(attachments.getJSONObject(j));
            }
            messages.add(m);
        }
        Collections.reverse(messages);
        return messages;
    }

    public static ArrayList<Buddy> userMapper(JSONArray array) {
        if (array == null)
            return null;
        ArrayList<Buddy> buddies = new ArrayList<>();
        for (int i = 0; i < array.length(); ++i) {
            Buddy buddy = new BuddyImpl();
            if (array.getJSONObject(i).has("id"))
                buddy.setUserId(array.getJSONObject(i).getInt("id"));
            buddy.setFirstName(array.getJSONObject(i).getString("first_name"));
            buddy.setLastName(array.getJSONObject(i).getString("last_name"));
            buddy.setAvatarURL(array.getJSONObject(i).getString("photo_50"));
            buddy.setOnlineStatus(array.getJSONObject(i).getInt("online") == 1 ?
                            OnlineStatus.ONLINE : OnlineStatus.OFFLINE
            );
            buddy.setStatus(array.getJSONObject(i).getString("status"));
            buddies.add(buddy);
        }
        return buddies;
    }

    public static void abortAllConnections() {
        networkClient.abort();
        longPollClient.abort();
    }

}
