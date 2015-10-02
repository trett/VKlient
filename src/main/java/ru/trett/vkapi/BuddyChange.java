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

package ru.trett.vkapi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Tretyakov
 * @since 24.09.15
 */
public class BuddyChange {

    private List<BuddyChangeSubscriber> buddyChangeSubscribers = new ArrayList<>(1);
    private OnlineStatus state;
    private int newMessages;
    private Buddy buddy;

    BuddyChange(Buddy buddy) {
        this.buddy = buddy;
    }

    public OnlineStatus getState() {
        return state;
    }

    public void setState(OnlineStatus state) {
        this.state = state;
        notifySubscribersAboutStatusChange();
    }

    public int getNewMessages() {
        return newMessages;
    }

    public void setNewMessages(int newMessages) {
        this.newMessages = newMessages;
        notifySubscribersAboutNewMessages();
    }

    public void attach(BuddyChangeSubscriber buddyChangeSubscriber) {
        buddyChangeSubscribers.add(buddyChangeSubscriber);
    }

    public void notifySubscribersAboutStatusChange() {
        for (BuddyChangeSubscriber buddyChangeSubscriber : buddyChangeSubscribers) {
            buddyChangeSubscriber.update(buddy);
        }
    }

    public void notifySubscribersAboutNewMessages() {
        for (BuddyChangeSubscriber buddyChangeSubscriber : buddyChangeSubscribers) {
            buddyChangeSubscriber.haveNewMessage(buddy);
        }
    }
}
