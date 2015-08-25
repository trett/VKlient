/**
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

package ru.trett.vklient;

import ru.trett.vkauth.Message;

/**
 *  Chat Window Interface.
 *
 */

public interface ChatWindow {

    /**
     * Return User ID of reciever
     * @return int
     */
    int getUserId();

    /**
     * Set the User ID of reciver
     * @param userId
     */

    void setUserId(int userId);

    /**
     * Return sender's account
     * @return Account
     */

    Account getAccount();

    /**
     * Set account for sender
     * @param account
     */

    void setAccount(Account account);

    /**
     * Append message to chat
     * @param message
     */
    void appendMessage(Message message);

    /**
     * Show Chat window
     *
     */
    void showWindow();

}
