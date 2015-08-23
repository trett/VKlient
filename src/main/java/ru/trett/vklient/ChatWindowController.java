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
import ru.trett.vkauth.VKUtils;

import java.text.SimpleDateFormat;
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
    }


    public void enterKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            if (!area.getText().isEmpty()) {
                String text = area.getText();
                String messageId = VKUtils.sendMessage(account, userId, text);
                appendMessage("[" + timeStamp + "] " + text, false);
                area.clear();
                area.positionCaret(0); //TODO: it seems doesn't work
            }
        }
    }

    public void showHistory() {
        String m = VKUtils.getMessagesHistory(account, userId, 50, 0);
        StringBuilder messages = new StringBuilder(m);
        messages.insert(0, style); //temporary
        messages.append("</div></body>");
        engine.loadContent(messages.toString());
    }

    public void appendMessage(String message, boolean incoming) {
        String direction = incoming ? "incomingMessage" : "outcomingMessage";
        engine.executeScript(
                "var newDiv = document.createElement('div');" +
                        "newDiv.setAttribute('id', '" + direction + "');" +
                        "newDiv.innerHTML ='" + message + "';" +
                        "document.getElementById('chat').appendChild(newDiv);");
    }
}
