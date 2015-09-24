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

import org.json.JSONObject;
import ru.trett.vkapi.Exceptions.RequestReturnErrorException;
import ru.trett.vkapi.Exceptions.RequestReturnNullException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */
public class Friends {

    /**
     * Get friends for given user_id
     *
     * @param userId int user_id
     * @param token String access_token
     * @return ArrayList buddies
     */
    public static ArrayList<Buddy> get(int userId, String token)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("user_id", Integer.toString(userId));
        urlParameters.put("access_token", token);
        urlParameters.put("fields", "first_name,last_name,photo_50,online,status");
        JSONObject obj = NetworkHelper.sendRequest("friends.get", urlParameters).getJSONObject("response");
        return new BuddyMapper().map(obj.getJSONArray("items"));
    }

}
