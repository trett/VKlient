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

import ru.trett.vkauth.Message;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

/**
 * Chat Window Interface.
 */

public interface ChatWindow {

    /**
     * Return User ID of reciever
     *
     * @return int
     */
    int getUserId();

    /**
     * Set the User ID of reciver
     *
     * @param userId
     */

    void setUserId(int userId);

    /**
     * Return sender's account
     *
     * @return Account
     */

    Account getAccount();

    /**
     * Set account for sender
     *
     * @param account
     */

    void setAccount(Account account);

    /**
     * Append message to chat
     *
     * @param message
     */
    void appendMessage(Message message);

    /**
     * Show Chat window
     */
    void showWindow();

    /**
     * Return true if windows are showing
     *
     * @return
     */
    public boolean isShowing();

}
