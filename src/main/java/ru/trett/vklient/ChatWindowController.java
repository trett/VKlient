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
import org.w3c.dom.Text;
import ru.trett.vkauth.Message;
import ru.trett.vkauth.VKUtils;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class ChatWindowController {

    @FXML
    private WebView view;
    @FXML
    private TextArea area;
    private WebEngine engine;
    private Account account = null;
    private int userId = 0;
    private Document doc;


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
            if (keyEvent.isShiftDown()) {
                area.appendText(System.getProperty("line.separator"));
            } else {
                String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
                if (!area.getText().isEmpty()) {
                    Message m = new Message();
                    m.setDate(timeStamp);
                    m.setBody(area.getText().replaceAll("<br>", "&lt;br&gt;"));
                    m.setDirection("out");
                    String messageId = VKUtils.sendMessage(account, userId, m);
                    area.setText("");
                    keyEvent.consume();
                }
            }
        }
    }

    private void showHistory() {
        ArrayList<Message> messages = VKUtils.getMessagesHistory(account, userId, 50, 0);
        if (messages != null)
            messages.forEach(x -> appendMessage(x));
    }

    public void appendMessage(Message message) {
        doc = engine.getDocument();
        String body = message.getBody().replaceAll("<br>", "\n"); // God fuck them all. Wrong character from LongPoll server
        Element el = doc.createElement("div");
        el.setAttribute("id", message.getDirection().contains("in") ? "incomingMessage" : "outcomingMessage");
        String[] splitString = body.split("\n");
        Text date = doc.createTextNode("[" + message.getDate() + "] ");
        el.appendChild(date);
        for (String part : splitString) {
            Text textNode = doc.createTextNode(unescapeHtml4(part));
            el.appendChild(textNode);
            el.appendChild(doc.createElement("br"));
        }
        doc.getElementById("chat").appendChild(el);
        engine.executeScript("scroll()");
    }

}