package ru.trett.vkauth;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;


public class BuddyImpl implements Buddy {

    private int userId = 0;
    private String firstName = "";
    private String lastName = "";
    private String avatarURL = "";
    private int online = 0;
    private String status = "";
    private IntegerProperty onlineStatusProperty = new SimpleIntegerProperty();

    @Override
    public final int getOnlineStatusProperty() {
        return onlineStatusProperty.get();
    }

    @Override
    public IntegerProperty onlineStatusProperty() {
        return onlineStatusProperty;
    }

    @Override
    public final void setOnlineStatusProperty(int onlineStatusProperty) {
        this.onlineStatusProperty.set(onlineStatusProperty);
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getAvatarURL() {
        return avatarURL;
    }

    @Override
    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    @Override
    public String getOnlineStatus() {
        return online == 0 ? "offline" : "online";
    }

    @Override
    public void setOnlineStatus(int online) {
        this.setOnlineStatusProperty(online);
        this.online = online;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    // TODO: Create sort by online Status
//    public int compareTo(BuddyImpl b) {
//        int onlineProp = b.getOnlineStatusProperty();
//        return this.onlineStatusProperty().intValue() - onlineProp;
//    }

}
