package ru.trett.vkauth;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */


import javafx.beans.property.IntegerProperty;

/**
 * Contains info about all Users
 */
public interface Buddy {

    int getUserId();

    void setUserId(int userId);

    String getLastName();

    void setLastName(String lastName);

    String getFirstName();

    void setFirstName(String firstName);

    String getAvatarURL();

    void setAvatarURL(String avatarURL);

    String getOnlineStatus();

    void setOnlineStatus(int online);

    String getStatus();

    void setStatus(String status);

    int getOnlineStatusProperty();

    void setOnlineStatusProperty(int onlineStatusProperty);

    IntegerProperty onlineStatusProperty();

    int getNewMessages();

    IntegerProperty newMessagesProperty();

    void setNewMessages(int newMessages);

}
