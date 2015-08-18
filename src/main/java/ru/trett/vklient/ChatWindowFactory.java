package ru.trett.vklient;

/**
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

public class ChatWindowFactory {
    public static ChatWindow getNewInstance(int userId, Account account) {
        return new ChatWindowImpl(userId, account);
    }
}
