/*
 *  *
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

package ru.trett.vklient;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import ru.trett.vkauth.Request;
import ru.trett.vkauth.VKUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class ChatWindowController {
    String lpServer = null;
    String key = null;
    int ts = 0;
    @FXML
    private WebView view;
    @FXML
    private TextArea area;
    private WebEngine engine;
    private Account account = null;
    private int userId = 0;


    public ChatWindowController() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @FXML
    private void initialize() {
        engine = view.getEngine();
    }


    public void enterKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            System.out.println("userId: ");
//            engine.loadContent(area.getText(), "text/html");
            try {

                String urlParameters = "user_id=" + URLEncoder.encode(Integer.toString(userId), "UTF-8") +
                        "&access_token=" + URLEncoder.encode(account.getAccessToken(), "UTF-8") +
                        "&chat_id=1" +
                        "&message=" + URLEncoder.encode(area.getText(), "UTF-8");
//                JSONObject obj = getInfo(userId, accessToken, "friends.get", urlParameters, "GET");
                String ans = Request.sendRequest("https://api.vk.com/method/messages.send", urlParameters);
//                System.out.println(account.getAccessToken() + " " + account.getUserId() + " " + userId + ans);
//                System.out.println(urlParameters);
                area.clear();
                area.positionCaret(0);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void showHistory() {
        String m = VKUtils.getMessagesHistory(account, userId, 50, 0);
        engine.loadContent(m);
    }

    //TODO: Connection to Long Poll Server
//    public void getUpdates() {
//        System.out.println("Long Poll Server: " + lpServer);
//        try {
//            String urlParameters = "act=a_check" + "key=" + URLEncoder.encode(key, "UTF-8") +
//                    "&ts=" + URLEncoder.encode(Integer.toString(ts), "UTF-8") +
//                    "&wait=25" +
//                    "&mode=2";
//            String m = Request.sendRequest("http://" + lpServer, urlParameters, "GET");
//            JSONObject obj = new JSONObject(m);
//            JSONArray array = obj.getJSONArray("response");
//            StringBuilder content = new StringBuilder();
//            for (int i = 0; i < array.length(); ++i) {
//                content.append(array.getJSONObject(i).getString("date"));
//                content.append(" ");
//                content.append(array.getJSONObject(i).getString("body"));
//                content.append(System.getProperty("line.separator"));
//            }
//            engine.loadContent(content.toString());
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void getLongPollServer() {
//        String hui = Request.sendRequest("https://api.vk.com/method/messages.getLongPollServer", "access_token=" + account.getAccessToken(), "GET");
//        System.out.println(hui);
//        JSONObject obj = new JSONObject(hui);
////        JSONArray array = obj.getJSONArray("response");
//        lpServer = obj.getJSONObject("response").getString("server");
//        key = obj.getJSONObject("response").getString("key");
//        ts = obj.getJSONObject("response").getInt("ts");
//    }
}
