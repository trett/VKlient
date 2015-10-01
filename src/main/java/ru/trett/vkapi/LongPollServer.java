/*
 * (C) Copyright Tretyakov Roman.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package ru.trett.vkapi;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vkapi.Exceptions.RequestReturnErrorException;
import ru.trett.vkapi.Exceptions.RequestReturnNullException;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

/**
 * @author Roman Tretyakov
 * @since 19.09.15
 */
public class LongPollServer {

    private static final NetworkClient longPollClient;

    static {
        longPollClient = new NetworkClient(26000);
    }

    private final String API_VERSION = "5.37";
    public BooleanProperty haveUpdates = new SimpleBooleanProperty(false);
    private boolean isOnline = false;
    private String lpServer = null;
    private String lpServerKey = null;
    private String ts = null;
    private JSONArray data;
    private NetworkClient networkClient;
    private Request request;
    private Account account;

    public LongPollServer(Account account) {
        networkClient = new NetworkClient(5000);
        this.account = account;
        System.out.println("Long Poll Connection has been created.");
    }

    public void start() {
        System.out.println("Long Poll Started");
        request = new RequestBuilder()
                .host("api.vk.com/method/")
                .path("messages.getLongPollServer")
                .query(
                        new WeakHashMap<String, String>() {{
                            put("access_token", account.getAccessToken());
                            put("v", API_VERSION);
                        }}
                )
                .build();
        getLongPollConnection();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                while (isOnline) {
                    setHaveUpdates(false);
                    try {
                        JSONObject json = getUpdates(lpServer, lpServerKey, ts);
                        ts = json.optString("ts");
                        data = json.getJSONArray("updates");
                        if (data.length() > 0)
                            setHaveUpdates(true);
                    } catch (RequestReturnNullException e) {
                        if(account.getOnlineStatus() != OnlineStatus.OFFLINE) {
                            account.connectionError(e);
                            System.out.println(e.getMessage());
                            isOnline = false;
                        }
                        return;
                    } catch (RequestReturnErrorException e) {
                        getLongPollConnection();
                    }
                }
            }
        };
        timer.schedule(timerTask, 2000);
    }

    public boolean getHaveUpdates() {
        return haveUpdates.get();
    }

    public void setHaveUpdates(boolean haveUpdates) {
        this.haveUpdates.set(haveUpdates);
    }

    public BooleanProperty haveUpdatesProperty() {
        return haveUpdates;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }

    private void getLongPollConnection() {
        try {
            JSONObject obj = new JSONObject(networkClient.send(request));
            lpServer = obj.getJSONObject("response").getString("server");
            lpServerKey = obj.getJSONObject("response").getString("key");
            ts = Integer.toString(obj.getJSONObject("response").getInt("ts"));
            isOnline = true;
        } catch (Exception e) {
            account.connectionError(e);
        }
    }

    public JSONObject getUpdates(final String server, final String key, final String ts)
            throws RequestReturnNullException, RequestReturnErrorException {
        try {
            Map<String, String> urlParameters = new WeakHashMap<>();
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
            if (obj.has("failed"))
                throw new RequestReturnErrorException("NetworkClient return error: "
                        + obj.getInt("failed"));
            return obj;
        } catch (ClientProtocolException e) {
            throw new RequestReturnNullException("Can't get Long Poll server cause: ", e);
        }
    }

    public void stop() {
        longPollClient.abort();
        System.out.println("Long Poll stopped.");
        isOnline = false;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

}
