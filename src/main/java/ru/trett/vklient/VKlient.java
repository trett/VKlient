package ru.trett.vklient;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.trett.vkauth.AuthHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class VKlient extends Application {

    private Stage mainStage;
    private Roster roster;

    public static void main(String[] args) {
        launch(args);
    }

    public static void setConfig(String key, String value) {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream("vklient.properties"));
            config.put(key, value);
            FileOutputStream out = new FileOutputStream("vklient.properties");
            config.store(out, "account settings");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getConfig(String key) {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream("vklient.properties"));
            String value = config.getProperty(key);
            return value;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        checkConfig();
        setUserAgentStylesheet(STYLESHEET_MODENA);
        mainStage = primaryStage;
        mainStage.setTitle("VKlient");
        roster = new Roster();
        mainStage.setScene(new Scene(roster.getRoot(), 300, 500));
        mainStage.getScene().getStylesheets().add("css/main.css");
//        Platform.setImplicitExit(false);
        if (getConfig("access_token") != null) {
            Account account = new Account();
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    account.setFriends();
                    Platform.runLater(() -> roster.setAccount(account));
                    account.setOnlineStatus(1);
                }
            };
            timer.schedule(timerTask, 3000);
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
                    Account account = new Account();
                    s.close();
                    roster.setAccount(account);
                    account.setOnlineStatus(1); //TODO: create ENUM for statusOnline
                });
        s.show();
    }

    private void checkConfig() {
        File configFile = new File("vklient.properties");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}