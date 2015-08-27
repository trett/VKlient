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
        if(chatWindows == null)
            chatWindows = new ArrayList<>();
        ChatWindowImpl chatWindow = new ChatWindowImpl(account, userId);
        chatWindows.add(chatWindow);
        return chatWindow;
    }
}
