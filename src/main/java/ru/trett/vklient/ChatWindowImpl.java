/*
 *  *
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

package ru.trett.vklient;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by maat on 16.08.15.
 */
public class ChatWindowImpl implements ChatWindow {

    public int userId = 0;
    public Account account = null;

    ChatWindowImpl(int userId, Account account) {
        this.userId = userId;
        this.account = account;
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/chatWindow.fxml"));
            root = loader.load();
//            root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/chatWindow.fxml"));
            ChatWindowController chatWindowController = loader.getController();
            chatWindowController.setAccount(account);
            chatWindowController.setUserId(userId);
            Stage stage = new Stage();
            stage.setTitle("Chat Window");
            stage.setScene(new Scene(root, 450, 450));
            stage.show();
//            chatWindowController.getLongPollServer();
            chatWindowController.showHistory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
