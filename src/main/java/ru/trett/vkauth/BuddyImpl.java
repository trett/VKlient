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

package ru.trett.vkauth;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class BuddyImpl implements Buddy {

    private int userId = 0;
    private String firstName = "";
    private String lastName = "";
    private String avatarURL = "";
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
    private String status = "";
    private IntegerProperty onlineStatusProperty = new SimpleIntegerProperty();
    private IntegerProperty newMessages = new SimpleIntegerProperty();

    @Override
    public final int getOnlineStatusProperty() {
        return onlineStatusProperty.get();
    }

    @Override
    public final void setOnlineStatusProperty(int onlineStatusProperty) {
        this.onlineStatusProperty.set(onlineStatusProperty);
    }

    @Override
    public IntegerProperty onlineStatusProperty() {
        return onlineStatusProperty;
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
        return onlineStatus.ordinal() == 0 ? "offline" : "online";
    }

    @Override
    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.setOnlineStatusProperty(onlineStatus.ordinal());
        this.onlineStatus = onlineStatus;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int getNewMessages() {
        return newMessages.get();
    }

    @Override
    public void setNewMessages(int newMessages) {
        this.newMessages.set(newMessages);
    }

    @Override
    public IntegerProperty newMessagesProperty() {
        return newMessages;
    }

    @Override
    public int compareTo(Buddy b) {
        if (this.getOnlineStatusProperty() > b.getOnlineStatusProperty()) {
            return -1;
        } else if (this.getOnlineStatusProperty() < b.getOnlineStatusProperty()) {
            return 1;
        } else {
            return 0;
        }
    }

}
