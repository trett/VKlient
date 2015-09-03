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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class AuthHelper {

    public static URL answer = null;
    // Application ID
    private final int CLIENT_ID = 5029224;
    private BooleanProperty isAnswerReceived = new SimpleBooleanProperty(false);

    /**
     * Split URL by key and value and return <b>Map&lt;key, value&gt;</b>
     *
     * @param url URL location
     * @return <b>Map&lt;key, value&gt;</b>
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        String query = url.getRef();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return queryPairs;
    }

    public final boolean getIsAnswerReceived() {
        return isAnswerReceived.get();
    }

    public final void setIsAnswerReceived(boolean isAnswerReceived) {
        this.isAnswerReceived.set(isAnswerReceived);
    }


    public BooleanProperty isAnswerReceivedProperty() {
        return isAnswerReceived;
    }

    private int getClient_id() {
        return CLIENT_ID;
    }

    /**
     * @return <b>Map&lt;key, value&gt;</b>
     */
    public Map<String, String> getAnswer() {
        if (!answer.toString().isEmpty()) {
            try {
                Map<String, String> list = AuthHelper.splitQuery(answer);
                return list;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @return Browser window for authentication
     */
    public AuthWindow createAuthWindow() {
        return new AuthWindow();
    }

    class AuthWindow extends Region {

        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        public AuthWindow() {
            Stage s = new Stage();
            s.setTitle("VKlient Authorization");
            s.setScene(new Scene(this, 750, 500, Color.web("#666970")));
            getStyleClass().add("browser");
            String url = "https://oauth.vk.com/authorize" +
                    "?client_id=" + getClient_id() +
                    "&response_type=token&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=69634";
            webEngine.load(url);
            getChildren().add(browser);
            webEngine.locationProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        if (newValue.contains("access_token")) {
                            try {
                                answer = new URL(webEngine.getLocation());
                                System.out.println(answer);
                                setIsAnswerReceived(true);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            s.show();
        }
    }
}

