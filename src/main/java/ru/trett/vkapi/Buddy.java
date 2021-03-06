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

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Buddy {

    private int userId;
    private String firstName = "";
    private String lastName = "";
    private String avatarURL = "";
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
    private String status = "";
    private BuddyChangeEvent buddyChangeEvent = new BuddyChangeEvent(this);

    /**
     * Get online status change notifier
     *
     * @return BuddyChangeEvent
     */
    public BuddyChangeEvent getBuddyChangeEvent() {
        return buddyChangeEvent;
    }

    public void setBuddyChangeEvent(BuddyChangeEvent buddyChangeEvent) {
        this.buddyChangeEvent = buddyChangeEvent;
    }

    /**
     * Return user_id
     *
     * @return int user_id
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Set user_id
     *
     * @param userId int user_id
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return String Buddy last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets Buddy last name
     *
     * @param lastName String last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return String first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets Buddy last name
     *
     * @param firstName String first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Return URL contains avatar
     *
     * @return URL with avatar Image
     */
    public String getAvatarURL() {
        return avatarURL;
    }

    /**
     * Sets avatar location
     *
     * @param avatarURL String location
     */
    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    /**
     * Return online status
     *
     * @return String "online" or "offline"
     */
    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    /**
     * Sets online status <br>
     * Possibble enum OnlineStatus
     *
     * @param onlineStatus int
     */
    public void setOnlineStatus(OnlineStatus onlineStatus, OnlineStatusReason reason) {
        this.getBuddyChangeEvent().setState(onlineStatus);
        this.onlineStatus = onlineStatus;
    }

    /**
     * @return String status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets Status text
     *
     * @param status String Status text
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sort by online status
     *
     * @param b Buddy
     * @return int
     */
    public int compareTo(Buddy b) {
        if (this.getBuddyChangeEvent().getState().ordinal() > b.getBuddyChangeEvent().getState().ordinal()) {
            return -1;
        } else if (this.getBuddyChangeEvent().getState().ordinal() < b.getBuddyChangeEvent().getState().ordinal()) {
            return 1;
        } else {
            return 0;
        }
    }

}
