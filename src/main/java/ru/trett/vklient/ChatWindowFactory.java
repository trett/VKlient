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

import java.util.ArrayList;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class ChatWindowFactory {

    private static ArrayList<ChatWindow> chatWindows;

    public static ArrayList<ChatWindow> getChatWindows() {
        return chatWindows;
    }

    /**
     * @param account
     * @param userId
     * @return existing window or null
     */
    public static ChatWindow getInstance(Account account, int userId) {
        if (chatWindows != null && chatWindows.size() > 0) {
            for (ChatWindow cw : chatWindows) {
                if (account == cw.getAccount() && userId == cw.getUserId())
                    return cw;
            }
        }
        return null;
    }

    public static ChatWindow createInstance(Account account, int userId) {
        if (chatWindows == null)
            chatWindows = new ArrayList<>();
        ChatWindowImpl chatWindow = new ChatWindowImpl(account, userId);
        chatWindows.add(chatWindow);
        return chatWindow;
    }
}
