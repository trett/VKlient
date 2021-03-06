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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import ru.trett.vkapi.*;
import ru.trett.vkapi.Exceptions.RequestReturnErrorException;
import ru.trett.vkapi.Exceptions.RequestReturnNullException;


/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Roster extends BuddyChangeSubscriber {

    private final GridPane root;
    private final IconLoader iconLoader;
    private final Config config = new Config();
    private final ComboBox<OnlineStatus> statusBox = new ComboBox<>();
    private Account account;
    private TreeItem<Buddy> me;
    private TreeItem<Buddy> friendsNode;
    private UpdatesHandler updatesHandler;
    private ObservableList<TreeItem<Buddy>> friendsModel = FXCollections.observableArrayList();
    private boolean rosterHideOffline = (Boolean.valueOf(
            config.getValue("rosterHideOffline", Boolean.toString(false))
    )
    );

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
            if (account != null) {
                saveSettings();
                if (account.getOnlineStatus() != OnlineStatus.OFFLINE)
                    account.setOnlineStatus(OnlineStatus.OFFLINE,
                            OnlineStatusReason.ON_DEMAND);
            }
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
        statusBox.setButtonCell(new StatusBoxCellImpl());
        statusBox.setCellFactory(listView -> new StatusBoxCellImpl());

        statusBox.setMinWidth(column.getMinWidth());
        statusBox.setPrefWidth(Double.MAX_VALUE);
        final ObservableList<OnlineStatus> status =
                FXCollections.observableArrayList(OnlineStatus.values());
        statusBox.getItems().addAll(status);
        root.add(mbar, 0, 0);
        root.add(statusBox, 0, 2);
        friendsNode = new TreeItem<>();
        try {
            addAccount();
        } catch (RequestReturnNullException e) {
            System.err.println("Network error.");
        } catch (RequestReturnErrorException e) {
            System.out.println(e.getMessage());
            authorize(new Account());
        }
        statusBox.setOnAction((ActionEvent event) -> {
            if (account == null)
                return;
            if (statusBox.getValue() != account.getOnlineStatus()) {
                Thread thread = new Thread(() -> {
                    account.setOnlineStatus(OnlineStatus.valueOf(statusBox.getValue().name()),
                            OnlineStatusReason.ON_DEMAND);
                });
                thread.start();
            }
        });
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
    public void createRootNode(Account account) {
        this.account = account;
        me = new TreeItem<>(account, iconLoader.getImageFromUrl(account.getAvatarURL()));
        final TreeView<Buddy> tree = new TreeView<>(me);
        tree.setCellFactory(call -> new BuddyCellFactoryImpl());
        root.add(tree, 0, 1);
        me.setExpanded(true);
        me.getChildren().add(friendsNode);
        account.getBuddyChangeEvent().attach(this);
        account.setOnlineStatus(OnlineStatus.valueOf(
                        config.getValue("lastStatus", OnlineStatus.OFFLINE.name()).toUpperCase()
                ), OnlineStatusReason.ON_DEMAND
        );
    }

    private void createFriendsNode() {
        if (account.getFriends() == null) {
            System.err.println("Can't create friends for null");
            return;
        }
        if (friendsModel.isEmpty()) {
            account.getFriends().forEach(x ->
                            friendsModel.add(new TreeItem<>(x, iconLoader.getImageFromUrl(x.getAvatarURL())))
            );
            friendsModel.forEach(x -> x.getValue().getBuddyChangeEvent().attach(this));
            friendsModel.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        }

        if (friendsNode.getChildren().isEmpty())
            friendsNode.getChildren().addAll(friendsModel);

        friendsNode.setExpanded(true);

        if (rosterHideOffline)
            hideOffline();

        if (updatesHandler == null)
            updatesHandler = new UpdatesHandler(account);
    }

    private ColorAdjust effect(OnlineStatus onlineStatus) {
        ColorAdjust colorAdjust = new ColorAdjust();
        switch (onlineStatus) {
            case OFFLINE:
                colorAdjust.setBrightness(-0.5);
                colorAdjust.setContrast(-0.5);
                break;
            default:
                colorAdjust.setBrightness(0);
                colorAdjust.setContrast(0);
                break;
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
        config.setValue("rosterWidth", Double.toString(root.getWidth()));
        config.setValue("rosterHeight", Double.toString(root.getHeight()));
    }

    private void addAccount() throws RequestReturnNullException, RequestReturnErrorException {
        if (config.getValue("access_token") != null && config.getValue("user_id") != null) {
            account = new Account();
            account.build(Integer.parseInt(config.getValue("user_id")),
                    config.getValue("access_token"));
            createRootNode(account);
        } else {
            authorize(new Account());
        }
    }

    private void authorize(Account account) {
        account.getAuthHelper();
        account.authFinishedProperty().addListener(
                (ObservableValue<? extends Boolean> observable, Boolean notFinished, Boolean finished) -> {
                    if (finished) {
                        config.setValue("access_token", account.getAccessToken());
                        config.setValue("user_id", Integer.toString(account.getUserId()));
                        createRootNode(account);
                    }
                });
        this.account = account;
    }

    @Override
    public void update(Buddy buddy) {
        TreeItem<Buddy> treeItem;
        if (buddy.getUserId() == account.getUserId()) {
            switch (buddy.getBuddyChangeEvent().getState()) {
                case OFFLINE:
                    friendsNode.getChildren().clear();
                    friendsModel.forEach(x -> x.getValue().getBuddyChangeEvent().remove(this));
                    friendsModel.clear();
                    break;
                case ONLINE:
                    createFriendsNode();
                    break;
                case INVISIBLE:
                    createFriendsNode();
            }
            Platform.runLater(() -> {
                        statusBox.setValue(account.getOnlineStatus());
                        me.getGraphic().setEffect(effect(account.getOnlineStatus()));
                    }
            );
            return;
        } else {
            System.out.println(buddy.getFirstName() + " changed status to " + buddy.getBuddyChangeEvent().getState().name());
            treeItem = getTreeItemByUserId(buddy.getUserId());
        }

        if (treeItem == null)
            return;

        Platform.runLater(() -> {
                    if (!rosterHideOffline) {
                        treeItem.getGraphic().setEffect(effect(buddy.getBuddyChangeEvent().getState()));
                    } else if (buddy.getBuddyChangeEvent().getState() == OnlineStatus.ONLINE) {
                        friendsNode.getChildren().add(treeItem);
                    } else {
                        friendsNode.getChildren().remove(treeItem);
                    }
                    friendsNode.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
                }
        );
    }

    @Override
    public void haveNewMessage(Buddy buddy) {
        TreeItem<Buddy> treeItem = getTreeItemByUserId(buddy.getUserId());
        if (treeItem == null)
            return;
        Platform.runLater(() -> {
            if (buddy.getBuddyChangeEvent().getNewMessages() > 0)
                treeItem.setGraphic(iconLoader.getIcon("unread", 32));
            else
                treeItem.setGraphic(iconLoader.getImageFromUrl(treeItem.getValue().getAvatarURL()));
            friendsNode.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue())); // temporary hack for update node
        });
    }

    public TreeItem<Buddy> getTreeItemByUserId(int userId) {
        TreeItem<Buddy> treeItem = null;
        for (TreeItem<Buddy> item : friendsModel) {
            if (item.getValue().getUserId() == userId)
                treeItem = item;
        }
        return treeItem == null ? null : treeItem;
    }

    private final class BuddyCellFactoryImpl extends TreeCell<Buddy> {

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
                    else if (!getItem().equals(me.getValue()))
                        ChatWindowFactory.createInstance(account, getItem().getUserId());
                    getItem().getBuddyChangeEvent().setNewMessages(0);
                }
            });

        }

        @Override
        public void updateItem(Buddy item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(getString());
                setGraphic(getTreeItem().getGraphic());
                getTreeItem().getGraphic().setEffect(effect(getItem().getBuddyChangeEvent().getState()));
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

    public final class StatusBoxCellImpl extends ListCell<OnlineStatus> {

        private final Circle circle;

        StatusBoxCellImpl() {
            circle = new Circle(7);
        }

        @Override
        protected void updateItem(OnlineStatus item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setGraphic(null);
            } else {
                switch (item) {
                    case ONLINE:
                        circle.setFill(Color.LIGHTGREEN);
                        setText("Online");
                        break;
                    case OFFLINE:
                        circle.setFill(Color.GRAY);
                        setText("Offline");
                        break;
                    case INVISIBLE:
                        circle.setFill(Color.YELLOWGREEN);
                        setText("Invisible");
                        break;
                }
                setGraphic(circle);
                getGraphic().setStyle(
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 12, 0, 1, 1);");
            }
        }
    }

}


