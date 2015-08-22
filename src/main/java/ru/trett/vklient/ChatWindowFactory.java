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

    public static ChatWindow getNewInstance(Account account, int userId) {
        if (chatWindows == null)
            chatWindows = new ArrayList<>();
        if (chatWindows.size() > 0) {
            for (ChatWindow cw : chatWindows) {
                if (account == cw.getAccount() && userId == cw.getUserId())
                    return cw;
            }
        }
        ChatWindowImpl chatWindow = new ChatWindowImpl(account, userId);
        chatWindows.add(chatWindow);
        return chatWindow;
    }
}
