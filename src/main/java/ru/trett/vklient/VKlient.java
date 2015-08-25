package ru.trett.vklient;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.trett.vkauth.AuthHelper;
import ru.trett.vkauth.VKUtils;

import java.util.Map;

public class VKlient extends Application {

    private Stage mainStage;
    private Roster roster;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Config config = new Config();
        config.checkStore();
        setUserAgentStylesheet(STYLESHEET_MODENA);
        mainStage = primaryStage;
        mainStage.setTitle("VKlient");
        roster = new Roster();
        mainStage.setScene(new Scene(roster.getRoot(), 300, 500));
        mainStage.getScene().getStylesheets().add("css/main.css");
        if (config.getValue("access_token") != null &&
                VKUtils.checkToken(config.getValue("access_token"))) {
            Account account = new Account();
            roster.setAccount(account);
        } else {
            authWindow();
        }
        mainStage.show();
    }

    private void authWindow() {
        AuthHelper helper = new AuthHelper();
        helper.showAuthWindow();
        helper.recievedAnswerProperty().addListener(
                (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    Config config = new Config();
                    Map<String, String> list = helper.getAnswer();
                    config.setValue("access_token", list.get("access_token"));
                    config.setValue("user_id", list.get("user_id"));
                    Account account = new Account();
                    roster.setAccount(account);
                });
    }
}