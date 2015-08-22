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
    private Stage stage;

    ChatWindowImpl(Account account, int userId) {
        this.userId = userId;
        this.account = account;
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/chatWindow.fxml"));
            root = loader.load();
            ChatWindowController chatWindowController = loader.getController();
            chatWindowController.setAccount(account);
            chatWindowController.setUserId(userId);
            stage = new Stage();
            stage.setTitle("Chat Window");
            stage.setScene(new Scene(root, 450, 450));
            stage.show();
            chatWindowController.showHistory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public void appendMessage(String message) {
        //TODO: realization
    }

    @Override
    public void showWindow() {
        if (!stage.isShowing())
            stage.show();
    }

}
