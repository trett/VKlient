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
import org.json.JSONArray;
import org.json.JSONObject;
import ru.trett.vkauth.Buddy;
import ru.trett.vkauth.Message;
import ru.trett.vkauth.VKUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

/**
 *
 * Parse udates array from answer
 */

public class UpdatesHandler {

    public static void update(JSONArray array, Account account) {
        for (int i = 0; i < array.length(); ++i) {
            JSONArray temp = array.getJSONArray(i);
            ArrayList<Object> list = new ArrayList<>();
            for (int j = 0; j < temp.length(); ++j) {
                list.add(temp.get(j));
            }
            //TODO: parse all !!!
            switch ((int) list.get(0)) {
                case 8:
                    account.getFriendById(account.getFriends(), -(int) list.get(1)).setOnlineStatus(1); //TODO:parse to platform
                    break;
                case 9:
                    account.getFriendById(account.getFriends(), -(int) list.get(1)).setOnlineStatus(0);
                    break;
                case 4:
                    Platform.runLater(() -> {
                        int flag = (int) list.get(2);

                        Message message = new Message();
                        /* if message have attachment get message from server by id cause long poll return useless answer */
                        if (!new JSONObject(list.get(7).toString()).isNull("attach1")) {
                            ArrayList<Message> messages = VKUtils.getMessagesById(account, (int) list.get(1));
                            if (messages != null)
                                message = messages.get(0);
                        } else {
                            Date date = new Date(Long.parseLong(list.get(4).toString()) * 1000);
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            message.setDate(sdf.format(date));
                            message.setBody(list.get(6).toString());
                            message.setDirection(
                                    (flag & VKUtils.MessageFlags.OUTBOX) == VKUtils.MessageFlags.OUTBOX ?
                                            "out" : "in"
                            );
                        }
                        ChatWindow chatWindow = ChatWindowFactory.getInstance(account, (int) list.get(3));
                        if (chatWindow != null && chatWindow.isShowing()) {
                            chatWindow.appendMessage(message);
                        } else if (chatWindow != null) {
                            Buddy b = account.getFriendById(account.getFriends(), (int) list.get(3));
                            b.setNewMessages(1);
                            chatWindow.appendMessage(message);
                            NotificationProvider.showNotification(b.getFirstName() + " " + b.getLastName());
                        } else {
                            Buddy b = account.getFriendById(account.getFriends(), (int) list.get(3));
                            b.setNewMessages(1);
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
