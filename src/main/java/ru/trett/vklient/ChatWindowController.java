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
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import ru.trett.vkapi.Account;
import ru.trett.vkapi.Exceptions.RequestReturnErrorException;
import ru.trett.vkapi.Exceptions.RequestReturnNullException;
import ru.trett.vkapi.Message;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;
import static ru.trett.vklient.ExternalBrowser.open;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class ChatWindowController {

    public static final String EVENT_TYPE_CLICK = "click";
    private static final String URL_REGEX = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    EventListener listener;
    @FXML
    private WebView view;
    @FXML
    private TextArea area;
    private WebEngine engine;
    private Account account;
    private int userId;
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
        URL htmlPath = getClass().getClassLoader().getResource("chat/chat.html");
        if (htmlPath == null)
            throw new RuntimeException("HTML Template was not found");
        engine.load(htmlPath.toExternalForm());
        engine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        listener = ev -> {
                            ev.preventDefault();
                            String domEventType = ev.getType();
                            if (domEventType.equals(EVENT_TYPE_CLICK)) {
                                String href = ((Element) ev.getTarget()).getAttribute("href");
                                open(href);
                            }
                        };
                        doc = engine.getDocument();
                        showHistory();
                        engine.executeScript("scroll();");
//                        engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
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
                    try {
                        String messageId = account.sendMessage(userId, m);
                    } catch (RequestReturnNullException | RequestReturnErrorException e) {
                        e.printStackTrace();
                    }
                    area.setText("");
                    keyEvent.consume();
                }
            }
        }
    }

    private void showHistory() {
        ArrayList<Message> messages = null;
        try {
            messages = account.getMessagesHistory(userId, 50, 0);
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
        }
        if (messages != null)
            messages.forEach(this::appendMessage);
    }

    public void appendMessage(final Message message) {
        String body = unescapeHtml4(message.getBody().replaceAll("<br>", "\n")); // Wrong character from LongPoll server
        Element el = doc.createElement("div");
        el.setAttribute("id", message.getDirection().contains("in") ? "incomingMessage" : "outgoingMessage");
        el.appendChild(doc.createElement("span"));
        Text date = doc.createTextNode("[" + message.getDate() + "] ");
        el.appendChild(date);
        String[] splitString = body.split("\n");
        for (String part : splitString) {
            if (part.matches(URL_REGEX)) {
                String[] words = body.split("\\s+");
                for (String word : words) {
                    if (word.matches(URL_REGEX)) {
                        Element url = doc.createElement("a");
                        url.setAttribute("href", word);
                        url.appendChild(doc.createTextNode(word));
                        el.appendChild(url);
                        ((EventTarget) url).addEventListener(EVENT_TYPE_CLICK, listener, false);
                    } else {
                        el.appendChild(doc.createTextNode(word));
                    }
                }
            } else {
                el.appendChild(doc.createTextNode(part));

            }
            el.appendChild(doc.createElement("br"));
        }

        if (message.getAttachments() != null) {
            Element div = doc.createElement("div");
            div.setAttribute("id", "attachment");
            for (Message.Attachment a : message.getAttachments()) {
                if (a.getTitle() != null) {
                    Element title = doc.createElement("span");
                    title.setAttribute("id", "title");
                    title.setTextContent(a.getTitle());
                    div.appendChild(title);
                }
                if (a.getPhoto() != null) {
                    Element image = doc.createElement("img");
                    image.setAttribute("src", a.getPhoto());
                    div.appendChild(image);
                }
                if (a.getUrl() != null) {
                    Element url = doc.createElement("a");
                    url.setAttribute("href", a.getUrl());
                    url.appendChild(doc.createTextNode(a.getUrl()));
                    ((EventTarget) url).addEventListener(EVENT_TYPE_CLICK, listener, false);
                    div.appendChild(url);
                }
                if (a.getDescription() != null) {
                    Element description = doc.createElement("span");
                    description.setAttribute("id", "description");
                    div.appendChild(description);
                }
            }
            el.appendChild(div);
        }
        doc.getElementById("chat").appendChild(el);
        engine.executeScript("scroll()");
    }

}