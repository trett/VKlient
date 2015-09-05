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

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import ru.trett.vkauth.Buddy;
import ru.trett.vkauth.VKUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Roster {
    private final GridPane root;
    TreeItem<Buddy> me;
    Account account;
    TreeItem<Buddy> friendsNode;
    TreeView<Buddy> tree;
    IconLoader iconLoader;
    ObservableList<TreeItem<Buddy>> friendsModel;
    boolean showOffline = true;

    Roster() {
        root = new GridPane();
        root.setGridLinesVisible(true);
        root.setHgap(2);
        root.setVgap(2);
        ColumnConstraints column = new ColumnConstraints(200, 300, Double.MAX_VALUE);
        column.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().add(column);
        RowConstraints row = new RowConstraints();
        RowConstraints row2 = new RowConstraints(400, 500, Double.MAX_VALUE);
        row2.setVgrow(Priority.ALWAYS);
        root.getRowConstraints().addAll(row, row2);
        MenuBar mbar = new MenuBar();
        Menu acc = new Menu();
        iconLoader = new IconLoader();
        acc.setGraphic(iconLoader.getIcon("vkontakte", 16));
        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction((ActionEvent event) -> {
            account.setOnlineStatus(VKUtils.OnlineStatus.OFFLINE);
            Platform.exit();
        });
        CheckMenuItem checkMenuItem = new CheckMenuItem("Hide Offline");
        checkMenuItem.setOnAction((ActionEvent event) -> {
            if (checkMenuItem.isSelected())
                hideOffline();
            else
                showOffline();
        });
        acc.getItems().addAll(checkMenuItem, quit);
        mbar.getMenus().addAll(acc);
        root.add(mbar, 0, 0);
        friendsNode = new TreeItem<>();
    }

    /**
     * @return Stage Root
     */
    public GridPane getRoot() {
        return root;
    }

    /**
     * Sets Account Me
     *
     * @param account Me
     */
    public void setAccount(Account account) {
        me = new TreeItem<>(account, IconLoader.getImageFromUrl(account.getAvatarURL()));
        me.setExpanded(true);
        this.account = account;
        fillFriendsNode();
    }

    private void fillFriendsNode() {
        me.getChildren().add(friendsNode);

        friendsModel = FXCollections.observableArrayList();
        account.getFriends().forEach(x ->
                        friendsModel.add(new TreeItem<>(x, IconLoader.getImageFromUrl(x.getAvatarURL())))
        );
        friendsNode.getChildren().addAll(friendsModel);
        friendsNode.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        friendsNode.getChildren().forEach(x -> {
            x.getValue().onlineStatusProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        System.out.println(x.getValue().getFirstName() + " change status to " + newValue.intValue());
                        if (showOffline) {
                            Platform.runLater(() ->
                                            x.getGraphic().setEffect(effect(newValue.intValue()))
                            );
                        } else if (newValue.intValue() == 1) {
                            friendsNode.getChildren().add(x);
                        } else {
                            friendsNode.getChildren().remove(x);
                        }
                        friendsNode.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
                        updateItems();
                    });
            x.getValue().newMessagesProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        if (newValue.intValue() > 0)
                            x.setGraphic(iconLoader.getIcon("unread", 32));
                        else
                            x.setGraphic(IconLoader.getImageFromUrl(x.getValue().getAvatarURL()));
                        updateItems();
                    });
        });
        friendsNode.setExpanded(true);
        tree = new TreeView<>(me);
        updateItems();
        root.add(tree, 0, 1, 1, 3);
    }

    private void updateItems() {
        tree.setCellFactory(call -> new BuddyCellFactoryImpl());
    }

    private ColorAdjust effect(int online) {
        ColorAdjust colorAdjust = new ColorAdjust();
        if (online == 0) {
            colorAdjust.setBrightness(-0.5);
            colorAdjust.setContrast(-0.5);
        } else {
            colorAdjust.setBrightness(0);
            colorAdjust.setContrast(0);
        }
        return colorAdjust;
    }

    private void hideOffline() {
        showOffline = false;
        friendsNode.getChildren().removeIf(buddyTreeItem -> buddyTreeItem.getValue().getOnlineStatus().contains("offline"));
    }

    private void showOffline() {
        showOffline = true;
        friendsModel.forEach(x -> {
            if (x.getValue().getOnlineStatus().contains("offline"))
                friendsNode.getChildren().add(x);
        });
    }

    private final class BuddyCellFactoryImpl extends TreeCell<Buddy> {

        private TextField textField;
        private ContextMenu addMenu = new ContextMenu();

        BuddyCellFactoryImpl() {
            setPrefWidth(150);
            MenuItem addMenuItem = new MenuItem("Send Message");
            addMenu.getItems().add(addMenuItem);
            setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
//            addMenuItem.setOnAction(new EventHandler() {
//                public void handle(Event t) {
//
//                }
//            });

            setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    ChatWindow chatWindow = ChatWindowFactory.getInstance(account, getItem().getUserId());
                    if (chatWindow != null)
                        chatWindow.showWindow();
                    else
                        ChatWindowFactory.createInstance(account, getItem().getUserId());
                    getItem().setNewMessages(0);
                }
            });

        }

        @Override
        public void updateItem(Buddy item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                    if (getItem() != null && getItem().getOnlineStatus() != null) {
                        getTreeItem().getGraphic().setEffect(effect(getItem().getOnlineStatusProperty()));
                    }
                }
            }
            setContextMenu(addMenu);
        }

        private String getString() {
            if (getItem() != null)
                return getItem().getFirstName() + " " + getItem().getLastName() +
                        System.getProperty("line.separator") + getItem().getStatus();
            return "";
        }
    }

}


