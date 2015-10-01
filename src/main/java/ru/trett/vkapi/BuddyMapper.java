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

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */
public class BuddyMapper {

    /**
     * Map JSONArray to ArrayList&lt;Buddy&gt;
     *
     * @param array JSONArray
     * @return ArrayList&lt;Buddy&gt;
     */
    public ArrayList<Buddy> map(JSONArray array) {
        ArrayList<Buddy> buddies = new ArrayList<>();
        for (int i = 0; i < array.length(); ++i) {
            Buddy buddy = new Buddy();
            if (array.getJSONObject(i).has("id"))
                buddy.setUserId(array.getJSONObject(i).getInt("id"));
            buddy.setFirstName(array.getJSONObject(i).getString("first_name"));
            buddy.setLastName(array.getJSONObject(i).getString("last_name"));
            buddy.setAvatarURL(array.getJSONObject(i).getString("photo_50"));
            buddy.setOnlineStatus(
                    array.getJSONObject(i).getInt("online") == 1 ?
                            OnlineStatus.ONLINE : OnlineStatus.OFFLINE,
                    OnlineStatusReason.BY_NETWORK_REQUEST
            );
            buddy.setStatus(array.getJSONObject(i).getString("status"));
            buddies.add(buddy);
        }
        return buddies;
    }
}
