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


/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

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

    void setNewMessages(int newMessages);

    IntegerProperty newMessagesProperty();

    int compareTo(Buddy b);

}
