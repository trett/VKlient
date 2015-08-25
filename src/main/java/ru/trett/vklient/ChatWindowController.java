/*
 *  *
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

package ru.trett.vklient;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.trett.vkauth.Message;
import ru.trett.vkauth.VKUtils;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ChatWindowController {

    final String style = "<head><style>" +
            "body {font: 10pt \"Liberation Sans\" \"Times New Roman\", Times, serif; -webkit-font-smoothing: antialiased; }" +
            "#incomingMessage { color: #008B00 }" +
            "#outcomingMessage { color: #191970}" +
            "</style></head><body><div id='chat'>";
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
        URL htmlPath = getClass().getClassLoader().getResource("chatStyle/chat.html");
        if (htmlPath == null)
            throw new RuntimeException("HTML Template was not found");
        engine.load(htmlPath.toExternalForm());
        engine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
//                        engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
                        showHistory();
                        engine.executeScript("scroll();");
                    }
                });
    }


    public void enterKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            if (!area.getText().isEmpty()) {
                String text = area.getText();
                Message m = new Message();
                m.setDate(timeStamp);
                m.setBody(text);
                m.setDirection("out");
                String messageId = VKUtils.sendMessage(account, userId, m);
                appendMessage(m);
                area.setText("");
                keyEvent.consume();
            }
        }
    }

    private void showHistory() {
        ArrayList<Message> messages = VKUtils.getMessagesHistory(account, userId, 50, 0);
        if (messages != null)
            messages.forEach(x -> appendMessage(x));
    }

    public void appendMessage(Message message) {
        Document doc = engine.getDocument();
        Element el = doc.createElement("div");
        if (message.getDirection() == "out")
            el.setAttribute("id", "outcomingMessage");
        else
            el.setAttribute("id", "incomingMessage");
        el.setTextContent("[" + message.getDate() + "] " + message.getBody());
        doc.getElementById("chat").appendChild(el);
        engine.executeScript("scroll();");
    }

}