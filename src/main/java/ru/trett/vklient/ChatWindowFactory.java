package ru.trett.vklient;

import java.util.ArrayList;

/**
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

public class ChatWindowFactory {

    private static ArrayList<ChatWindow> chatWindows;

    public static ArrayList<ChatWindow> getChatWindows() {
        return chatWindows;
    }

    public static ChatWindow getNewInstance(Account account, int userId) {
        ChatWindowImpl chatWindow = new ChatWindowImpl(account, userId);
        chatWindows = new ArrayList<>();
        chatWindows.add(chatWindow);
        return chatWindow;
    }
}
