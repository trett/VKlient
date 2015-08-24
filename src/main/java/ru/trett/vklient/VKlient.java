package ru.trett.vklient;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.trett.vkauth.AuthHelper;

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
//        Platform.setImplicitExit(false);
        if (config.getValue("access_token") != null) {
            Account account = new Account();
            roster.setAccount(account);
        } else {
            showAuthWindow();
        }

        mainStage.show();
    }

    private void showAuthWindow() {
        Stage s = new Stage();
        AuthHelper helper = new AuthHelper();
        s.setTitle("VKlient Authorization");
        s.setScene(new Scene(helper.getAuthWindow(), 750, 500, Color.web("#666970")));
        helper.recievedAnswerProperty().addListener(
                (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    Config config = new Config();
                    Map<String, String> list = helper.getAnswer();
                    config.setValue("access_token", list.get("access_token"));
                    config.setValue("user_id", list.get("user_id"));
                    Account account = new Account();
                    s.close();
                    roster.setAccount(account);
                });
        s.show();
    }

}