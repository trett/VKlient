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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Roman Tretyakov
 * @since 19.09.15
 */
public class LongPollServer {

    public BooleanProperty haveUpdates = new SimpleBooleanProperty(false);
    private String lpServer = null;
    private String lpServerKey = null;
    private String ts = null;
    private Account account;
    private JSONArray updates;

    public LongPollServer(Account account) {
        this.account = account;
        getLongPollConnection();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                while (account.getOnlineStatus() != OnlineStatus.OFFLINE) {
                    setHaveUpdates(false);
                    try {
                        JSONObject json = VKUtils.getUpdates(lpServer, lpServerKey, ts);
                        assert json != null;
                        ts = json.optString("ts");
                        updates = json.getJSONArray("updates");
                        if (updates.length() > 0)
                            setHaveUpdates(true);
                    } catch (RequestReturnNullException e) {
                        System.out.println(e.getMessage());
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

    public JSONArray getUpdates() {
        return updates;
    }

    public void setUpdates(JSONArray updates) {
        this.updates = updates;
    }

    private void getLongPollConnection() {
        HashMap<String, String> longPollServer = VKUtils.getLongPollServer(account.getAccessToken());
        if (longPollServer == null)
            throw new RuntimeException("Can't get long poll server.");
        lpServer = longPollServer.get("server");
        lpServerKey = longPollServer.get("key");
        ts = longPollServer.get("ts");
    }

}
