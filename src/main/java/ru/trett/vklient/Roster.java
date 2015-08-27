package ru.trett.vklient;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import ru.trett.vkauth.Buddy;
import ru.trett.vkauth.VKUtils;

public class Roster {
    private final GridPane root;
    TreeItem<Buddy> me;
    Account account;
    TreeItem<Buddy> friendsNode;
    TreeView<Buddy> tree;
    IconLoader iconLoader;

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
        acc.getItems().add(quit);
        Menu set = new Menu("Settings");
        mbar.getMenus().addAll(acc, set);
        root.add(mbar, 0, 0);
        friendsNode = new TreeItem<>();
    }

    public GridPane getRoot() {
        return root;
    }

    public void setAccount(Account account) {
        me = new TreeItem<>(account, IconLoader.getImageFromUrl(account.getAvatarURL()));
        me.setExpanded(true);
        this.account = account;
        fillFriendsNode();
    }

    public void fillFriendsNode() {
        me.getChildren().add(friendsNode);
        account.getFriends().forEach(x -> {
            TreeItem<Buddy> buddy = new TreeItem<>(x, IconLoader.getImageFromUrl(x.getAvatarURL()));
            x.onlineStatusProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        System.out.println(x.getFirstName() + " change status to " + newValue.intValue());
                        buddy.getGraphic().setEffect(effect(newValue.intValue()));
                        friendsNode.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
                        updateItems();
                    });
            x.newMessagesProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        if (newValue.intValue() > 0)
                            buddy.setGraphic(iconLoader.getIcon("unread", 32));
                        else
                            buddy.setGraphic(IconLoader.getImageFromUrl(buddy.getValue().getAvatarURL()));
                        updateItems();
                    });
            friendsNode.getChildren().add(buddy);
//            friendsNode.getChildren().sort((o1, o2) ->
//                    o1.getValue().getFirstName().compareTo(o2.getValue().getFirstName()));
            friendsNode.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        });

        friendsNode.setExpanded(true);
        tree = new TreeView<>(me);
        updateItems();
        root.add(tree, 0, 1, 1, 3);
    }

    private void updateItems() {
        tree.setCellFactory(call -> {
            return new BuddyCellFactoryImpl();
        });
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

    private final class BuddyCellFactoryImpl extends TreeCell<Buddy> {

        private TextField textField;
        private ContextMenu addMenu = new ContextMenu();

        BuddyCellFactoryImpl() {
            MenuItem addMenuItem = new MenuItem("Send Message");
            addMenu.getItems().add(addMenuItem);
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


