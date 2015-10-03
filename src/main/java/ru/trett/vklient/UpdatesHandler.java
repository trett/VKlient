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
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vkapi.*;
import ru.trett.vkapi.Exceptions.RequestReturnErrorException;
import ru.trett.vkapi.Exceptions.RequestReturnNullException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

/**
 * Parse updates array from answer
 */

public class UpdatesHandler {

    private Account account;

    UpdatesHandler(Account account) {
        this.account = account;
        account.getLongPollServer().haveUpdatesProperty().addListener(
                (ObservableValue<? extends Boolean> observable, Boolean noUpdates, Boolean haveUpdates) -> {
                    if (haveUpdates)
                        update(account.getLongPollServer().getData());
                });
    }

    public void update(JSONArray array) {
        for (int i = 0; i < array.length(); ++i) {
            JSONArray temp = array.getJSONArray(i);
            ArrayList<Object> list = new ArrayList<>(1);
            for (int j = 0; j < temp.length(); ++j) {
                list.add(temp.get(j));
            }
            //TODO: parse all !!!
            switch ((int) list.get(0)) {
                case 8:
                    account.getFriendById(account.getFriends(), -(int) list.get(1))
                            .setOnlineStatus(OnlineStatus.ONLINE,
                                    OnlineStatusReason.BY_NETWORK_REQUEST); //TODO:parse to platform
                    break;
                case 9:
                    account.getFriendById(account.getFriends(), -(int) list.get(1))
                            .setOnlineStatus(OnlineStatus.OFFLINE,
                                    OnlineStatusReason.BY_NETWORK_REQUEST);
                    break;
                case 4:
                    Platform.runLater(() -> {
                        int flag = (int) list.get(2);

                        Message message = new Message();
                        /* if message have attachment get message from server by id
                         cause long poll return useless answer */
                        if (!new JSONObject(list.get(7).toString()).isNull("attach1")) {
                            ArrayList<Message> messages = null;
                            try {
                                messages = account.getMessagesById((int) list.get(1));
                            } catch (RequestReturnNullException | RequestReturnErrorException e) {
                                account.connectionError(e);
                            }
                            if (messages != null)
                                message = messages.get(0);
                        } else {
                            Date date = new Date(Long.parseLong(list.get(4).toString()) * 1000);
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            message.setDate(sdf.format(date));
                            message.setBody(list.get(6).toString());
                            message.setDirection(
                                    (flag & Message.MessageFlags.OUTBOX) == Message.MessageFlags.OUTBOX ?
                                            "out" : "in"
                            );
                        }
                        ChatWindow chatWindow = ChatWindowFactory.getInstance(account, (int) list.get(3));
                        if (chatWindow != null && chatWindow.isShowing()) {
                            chatWindow.appendMessage(message);
                        } else {
                            Buddy b = account.getFriendById(account.getFriends(), (int) list.get(3));
                            b.getBuddyChange().setNewMessages(1);
                            NotificationProvider.showNotification(b.getFirstName() + " " + b.getLastName());
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }
}
