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

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import ru.trett.vkapi.Account;
import ru.trett.vkapi.Buddy;
import ru.trett.vkapi.Exceptions.RequestReturnNullException;
import ru.trett.vkapi.Exceptions.TokenErrorException;
import ru.trett.vkapi.OnlineStatus;
import ru.trett.vkapi.Users;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Roster {

    private final GridPane root;
    private final IconLoader iconLoader;
    private final Config config = new Config();
    private final ComboBox<OnlineStatus> statusBox = new ComboBox<>();
    private Account account;
    private TreeItem<Buddy> friendsNode;
    private TreeView<Buddy> tree;
    private ObservableList<TreeItem<Buddy>> friendsModel = FXCollections.observableArrayList();
    private boolean rosterHideOffline = (Boolean.valueOf(
            config.getValue("rosterHideOffline", Boolean.toString(false))
    )
    );
    private UpdatesHandler updatesHandler;

    Roster() {
        root = new GridPane();
        final MenuBar mbar = new MenuBar();
        final Menu main = new Menu();
        final MenuItem quit = new MenuItem("Quit");
        final CheckMenuItem hideOffline = new CheckMenuItem("Hide Offline");
        final ColumnConstraints column = new ColumnConstraints(200, 300, Double.MAX_VALUE);
        final RowConstraints row = new RowConstraints();
        final RowConstraints row2 = new RowConstraints(50, 200, Double.MAX_VALUE);
        final RowConstraints row3 = new RowConstraints();
        column.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().add(column);
        row2.setVgrow(Priority.ALWAYS);
        root.getRowConstraints().addAll(row, row2, row3);
        iconLoader = new IconLoader();
        main.setGraphic(iconLoader.getIcon("vkontakte", 16));
        quit.setOnAction((ActionEvent event) -> {
            saveSettings();
            account.setOnlineStatus(OnlineStatus.OFFLINE);
            Platform.exit();
        });
        hideOffline.setOnAction((ActionEvent event) -> {
            if (hideOffline.isSelected())
                hideOffline();
            else
                showOffline();
        });
        main.getItems().addAll(hideOffline, quit);
        mbar.getMenus().addAll(main);
        statusBox.setMinWidth(column.getMinWidth());
        statusBox.setPrefWidth(Double.MAX_VALUE);
        ObservableList<OnlineStatus> status = FXCollections.observableArrayList(OnlineStatus.values());
        statusBox.getItems().addAll(status);
        root.add(mbar, 0, 0);
        root.add(statusBox, 0, 2);
        friendsNode = new TreeItem<>();
        createRootNode();
        statusBox.setOnAction((ActionEvent event) -> {
            Thread thread = new Thread(() -> {
                account.setOnlineStatus(OnlineStatus.valueOf(statusBox.getValue().name()));
            });
            thread.start();
        });
        statusBox.setValue(
                OnlineStatus.valueOf(
                        config.getValue("lastStatus", OnlineStatus.OFFLINE.name()).toUpperCase()
                )
        );
        hideOffline.setSelected(rosterHideOffline);
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
    public void addAccount(Account account) {
        this.account = account;
        TreeItem<Buddy> me = new TreeItem<>(account, IconLoader.getImageFromUrl(account.getAvatarURL()));
        tree = new TreeView<>(me);
        root.add(tree, 0, 1);
        me.setExpanded(true);
        me.getChildren().add(friendsNode);
        updateItems();
        account.onlineStatusProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldStatus, Number newStatus) -> {
                    Platform.runLater(() -> {
                        switch (newStatus.intValue()) {
                            case 0:
                                statusBox.setValue(OnlineStatus.OFFLINE);
                                setState(OnlineStatus.OFFLINE);
                                break;
                            case 1:
                                statusBox.setValue(OnlineStatus.ONLINE);
                                setState(OnlineStatus.ONLINE);
                                break;
                            case 2:
                                statusBox.setValue(OnlineStatus.INVISIBLE);
                                setState(OnlineStatus.INVISIBLE);
                        }
                    });
                });

        account.setOnlineStatus(OnlineStatus.valueOf(
                        config.getValue("lastStatus", OnlineStatus.OFFLINE.name()).toUpperCase()
                )
        );
    }

    private void createFriendsNode() {
        account.getFriends().forEach(x ->
                        friendsModel.add(new TreeItem<>(x, IconLoader.getImageFromUrl(x.getAvatarURL())))
        );
        friendsNode.getChildren().addAll(friendsModel);
        friendsNode.getChildren().forEach(x -> {
            x.getValue().onlineStatusProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldStatus, Number newStatus) -> {
                        System.out.println(x.getValue().getFirstName() + " change status to " + newStatus.intValue());
                        Platform.runLater(() -> {
                                    if (!rosterHideOffline) {
                                        x.getGraphic().setEffect(effect(newStatus.intValue()));
                                    } else if (newStatus.intValue() == 1) {
                                        friendsNode.getChildren().add(x);
                                    } else {
                                        friendsNode.getChildren().remove(x);
                                    }
                                    friendsNode.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
                                    updateItems();
                                }
                        );
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
        friendsNode.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        Platform.runLater(() -> friendsNode.setExpanded(true));
        if (rosterHideOffline)
            hideOffline();
        updateItems();
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
        rosterHideOffline = true;
        friendsNode.getChildren().removeIf(
                buddyTreeItem -> buddyTreeItem.getValue().getOnlineStatus().name().contains("OFFLINE")
        );
    }

    private void showOffline() {
        rosterHideOffline = false;
        friendsModel.forEach(x -> {
            if (x.getValue().getOnlineStatus().name().contains("OFFLINE"))
                friendsNode.getChildren().add(x);
        });
    }

    private void saveSettings() {
        config.setValue("lastStatus", account.getOnlineStatus().name());
        config.setValue("rosterHideOffline", Boolean.toString(rosterHideOffline));
    }

    private void createRootNode() {
        account = new Account();
        if (config.getValue("access_token") != null) {
            try {
                Users.get(config.getValue("access_token"));
                account.create(Integer.parseInt(config.getValue("user_id")),
                        config.getValue("access_token"));
                addAccount(account);
            } catch (TokenErrorException e) {
                System.out.println(e.getMessage());
                authorize(account);
            } catch (RequestReturnNullException e) {
                System.out.println("Network error: " + e.getMessage());
            }
        } else {
            authorize(account);
        }
    }

    private void authorize(Account account) {
        account.getAuthHelper();
        account.authFinishedProperty().addListener(
                (ObservableValue<? extends Boolean> observable, Boolean notFinished, Boolean finished) -> {
                    if (finished) {
                        addAccount(account);
                        config.setValue("access_token", account.getAccessToken());
                        config.setValue("user_id", Integer.toString(account.getUserId()));
                        addAccount(account);
                    }
                });
    }

    private void setState(OnlineStatus onlineStatus) {
        switch (onlineStatus) {
            case OFFLINE:
                Platform.runLater(() -> friendsNode.getChildren().removeAll(friendsNode.getChildren()));
                break;
            case ONLINE:
                if (updatesHandler == null)
                    updatesHandler = new UpdatesHandler(account);
                if (friendsModel.isEmpty()) {
                    createFriendsNode();
                } else if (friendsNode.getChildren().isEmpty()) {
                    friendsNode.getChildren().addAll(friendsModel);
                    if (rosterHideOffline)
                        hideOffline();
                    updateItems();
                }
                break;
            case INVISIBLE:
                if (updatesHandler == null)
                    updatesHandler = new UpdatesHandler(account);
                if (friendsModel.isEmpty()) {
                    createFriendsNode();
                } else if (friendsNode.getChildren().isEmpty()) {
                    friendsNode.getChildren().addAll(friendsModel);
                    if (rosterHideOffline)
                        hideOffline();
                    updateItems();
                }
                break;
        }
    }

    private final class BuddyCellFactoryImpl extends TreeCell<Buddy> {

        private TextField textField;
        private ContextMenu addMenu = new ContextMenu();

        BuddyCellFactoryImpl() {
            setPrefWidth(150);
            setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
//            MenuItem addMenuItem = new MenuItem("Something");
//            addMenu.getItems().add(addMenuItem);
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


