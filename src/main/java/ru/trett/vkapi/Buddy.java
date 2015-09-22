/*
 * (C) Copyright Tretyakov Roman.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package ru.trett.vkapi;

import javafx.beans.property.IntegerProperty;


/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

/**
 * Contains info about all Users
 */
public interface Buddy {

    /**
     * Return user_id
     *
     * @return int user_id
     */
    int getUserId();

    /**
     * Set user_id
     *
     * @param userId int user_id
     */
    void setUserId(int userId);

    /**
     * @return String Buddy last name
     */
    String getLastName();

    /**
     * Sets Buddy last name
     *
     * @param lastName String last name
     */
    void setLastName(String lastName);

    /**
     * @return String first name
     */
    String getFirstName();

    /**
     * Sets Buddy last name
     *
     * @param firstName String first name
     */
    void setFirstName(String firstName);

    /**
     * Return URL contains avatar
     *
     * @return URL with avatar Image
     */
    String getAvatarURL();

    /**
     * Sets avatar location
     *
     * @param avatarURL String location
     */
    void setAvatarURL(String avatarURL);

    /**
     * Return online status
     *
     * @return String "online" or "offline"
     */
    OnlineStatus getOnlineStatus();

    /**
     * Sets online status <br>
     * Possibble enum OnlineStatus
     *
     * @param onlineStatus int
     */
    void setOnlineStatus(OnlineStatus onlineStatus);

    /**
     * @return String status
     */
    String getStatus();

    /**
     * Sets status
     *
     * @param status String status
     */
    void setStatus(String status);

    int getOnlineStatusProperty();

    void setOnlineStatusProperty(int onlineStatusProperty);

    IntegerProperty onlineStatusProperty();

    int getNewMessages();

    void setNewMessages(int newMessages);

    IntegerProperty newMessagesProperty();

    /**
     * Sort by online status
     *
     * @param b Buddy
     * @return int
     */
    int compareTo(Buddy b);

}
