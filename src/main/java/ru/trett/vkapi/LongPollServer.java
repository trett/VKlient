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
import java.util.WeakHashMap;

/**
 * @author Roman Tretyakov
 * @since 19.09.15
 */
public class LongPollServer {

    private static final NetworkClient longPollClient;
    private static final NetworkClient networkClient;

    static {
        longPollClient = new NetworkClient(26000);
        networkClient = new NetworkClient(5000);
    }

    private final String API_VERSION = "5.37";
    public BooleanProperty haveUpdates = new SimpleBooleanProperty(false);
    private boolean isOnline;
    private String lpServer;
    private String lpServerKey;
    private String ts;
    private JSONArray data;
    private Request request;
    private Account account;
    private Thread thread;

    public LongPollServer(Account account) {
        this.account = account;
        System.out.println("Long Poll Connection has been created.");
    }

    public void start() {
        if (isOnline)
            return;
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
        thread = new Thread(() -> {
            while (isOnline) {
                setHaveUpdates(false);
                try {
                    JSONObject json = getUpdates(lpServer, lpServerKey, ts);
                    ts = json.optString("ts");
                    data = json.getJSONArray("updates");
                    if (data.length() > 0)
                        setHaveUpdates(true);
                } catch (RequestReturnNullException e) {
                    if (account.getOnlineStatus() != OnlineStatus.OFFLINE) {
                        account.connectionError(e);
                        System.out.println(e.getMessage());
                        isOnline = false;
                    }
                    return;
                } catch (RequestReturnErrorException e) {
                    getLongPollConnection();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
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
                throw new RequestReturnNullException("Long Poll server return null.");
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
        if (!isOnline)
            return;
        longPollClient.abort();
        System.out.println("Long Poll stopped.");
        isOnline = false;
        thread.interrupt();
    }

}
