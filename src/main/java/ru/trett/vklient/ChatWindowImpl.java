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
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import ru.trett.vkauth.Buddy;
import ru.trett.vkauth.Message;

import java.io.IOException;

/**
 * Created by maat on 16.08.15.
 */
public class ChatWindowImpl implements ChatWindow {

    public int userId = 0;
    public Account account = null;
    private Stage stage;
    private ChatWindowController chatWindowController;

    ChatWindowImpl(Account account, int userId) {
        this.userId = userId;
        this.account = account;
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/chatWindow.fxml"));
            root = loader.load();
            chatWindowController = loader.getController();
            chatWindowController.setAccount(account);
            chatWindowController.setUserId(userId);
            Buddy user = account.getFriendById(account.getFriends(), userId);
            stage = new Stage();
            stage.setTitle("Chat with " + user.getFirstName() + " " + user.getLastName());
            ImageView n = (ImageView) IconLoader.getImageFromUrl(user.getAvatarURL());
            stage.getIcons().add(n.getImage());
            stage.setScene(new Scene(root, 450, 450));
            stage.show();
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
    public void appendMessage(Message message) {
        chatWindowController.appendMessage(message);
    }

    @Override
    public void showWindow() {
        if (!stage.isShowing())
            stage.show();
    }

    public boolean isShowing() {
        return stage.isShowing();
    }

}
