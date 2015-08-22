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

public class VKlient extends Application {

    private Stage mainStage;
    private Roster roster;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        mainStage = primaryStage;
        mainStage.setTitle("VKlient");
        roster = new Roster();
        mainStage.setScene(new Scene(roster.getRoot(), 300, 500));
        mainStage.getScene().getStylesheets().add("css/main.css");
        mainStage.close();
//        Platform.setImplicitExit(false);
        showAuthWindow();
        mainStage.show();
    }


    private void showAuthWindow() {
        Stage s = new Stage();
        AuthHelper helper = new AuthHelper();
        s.setTitle("VKlient Authorization");
        s.setScene(new Scene(helper.getAuthWindow(), 750, 500, Color.web("#666970")));
        helper.recievedAnswerProperty().addListener(
                (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    Account account = new Account();
                    s.close();
                    roster.setAccount(account);
                    account.setOnlineStatus(1); //TODO: create ENUM for statusOnline
                });
        s.show();
    }
}