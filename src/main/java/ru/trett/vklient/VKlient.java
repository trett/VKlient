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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class VKlient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        Config config = new Config();
        config.checkStore();
        primaryStage.setTitle("VKlient");
        final Roster roster = new Roster();
        primaryStage.setScene(new Scene(roster.getRoot(),
                Double.parseDouble(config.getValue("rosterWidth", "200")),
                Double.parseDouble(config.getValue("rosterHeight", "400"))
        ));
        primaryStage.setMinHeight(200);
        primaryStage.setMinWidth(200);
        try {
            final Image appIcon = new Image(getClass().getClassLoader().getResourceAsStream("vklient.png"));
            primaryStage.getIcons().add(appIcon);
        } catch (NullPointerException e) {
            System.out.println("Application icon not found");
        }
        primaryStage.getScene().getStylesheets().add("css/main.css");
        primaryStage.show();
    }
}