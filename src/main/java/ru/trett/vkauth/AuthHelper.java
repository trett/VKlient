package ru.trett.vkauth;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import ru.trett.vklient.VKlient;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthHelper {

    public static URL answer = null;
    // Application ID
    private final int CLIENT_ID = 5029224;
    private BooleanProperty recievedAnswer = new SimpleBooleanProperty(false);

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

    public final boolean getRecievedAnswer() {
        return recievedAnswer.get();
    }

    public final void setRecievedAnswer(boolean recievedAnswer) {
        this.recievedAnswer.set(recievedAnswer);
    }

    public BooleanProperty recievedAnswerProperty() {
        return recievedAnswer;
    }

    public int getClient_id() {
        return CLIENT_ID;
    }

    public Map<String, String> getToken() {
        if(VKlient.getConfig("access_token") != null) {
            Map<String, String> list = new HashMap<>();
            list.put("access_token", VKlient.getConfig("access_token"));
            list.put("user_id", VKlient.getConfig("user_id"));
            return list;
        }
        if (!answer.toString().isEmpty()) {
            try {
                Map<String, String> list = AuthHelper.splitQuery(answer);
                VKlient.setConfig("access_token", list.get("access_token"));
                VKlient.setConfig("user_id", list.get("user_id"));
                return list;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public AuthWindow getAuthWindow() {
        return new AuthWindow();
    }

    class AuthWindow extends Region {

        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        public AuthWindow() {
            getStyleClass().add("browser");
            StringBuilder url = new StringBuilder("https://oauth.vk.com/authorize");
            url.append("?client_id=" + getClient_id());
            url.append("&response_type=token&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=4098");
            webEngine.load(url.toString());
            getChildren().add(browser);
            webEngine.locationProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        if (newValue.contains("access_token")) {
                            try {
                                answer = new URL(webEngine.getLocation()); //TODO: timer on expires_in
                                System.out.println(answer);
                                setRecievedAnswer(true);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    });

        }
    }
}

