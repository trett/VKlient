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

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.trett.vkauth.AuthHelper;
import ru.trett.vkauth.VKUtils;

import java.util.Map;


/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class VKlient extends Application {

    private Roster roster;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Config config = new Config();
        config.checkStore();
        setUserAgentStylesheet(STYLESHEET_MODENA);
        primaryStage.setTitle("VKlient");
        roster = new Roster();
        primaryStage.setScene(new Scene(roster.getRoot(), 200, 400));
        try {
            Image appIcon = new Image(getClass().getClassLoader().getResourceAsStream("vklient.png"));
            primaryStage.getIcons().add(appIcon);
        } catch (NullPointerException e) {
            System.out.println("Application icon not found");
        }
        primaryStage.getScene().getStylesheets().add("css/main.css");
        if (config.getValue("access_token") != null &&
                VKUtils.checkToken(config.getValue("access_token"))) {
            Account account = new Account();
            roster.setAccount(account);
        } else {
            authWindow();
        }
        primaryStage.show();
    }

    private void authWindow() {
        AuthHelper helper = new AuthHelper();
        helper.createAuthWindow();
        helper.isAnswerReceivedProperty().addListener(
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