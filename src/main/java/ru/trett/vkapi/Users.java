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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */
public class Users {

    /**
     * Get users by id
     *
     * @param userIds List of Integer ids
     * @param token   String
     * @return ArrayList buddies
     */
    public static ArrayList<Buddy> get(List<Integer> userIds, String token)
            throws RequestReturnNullException, RequestReturnErrorException {
        HashMap<String, String> urlParameters = new HashMap<>();
        String ids = userIds.stream().map(x -> x.toString()).collect(Collectors.joining(","));
        urlParameters.put("user_ids", ids);
        urlParameters.put("access_token", token);
        urlParameters.put("fields", "photo_50,online,status");
        JSONObject obj = NetworkHelper.sendRequest("users.get", urlParameters);
        return new BuddyMapper().map(obj.getJSONArray("response"));
    }

    /**
     * Return user_id for given token
     *
     * @param token
     * @return int user_id or 0 if token wrong
     */
    public static int get(String token) {
        try {
            HashMap<String, String> urlParameters = new HashMap<>();
            urlParameters.put("access_token", token);
            JSONObject json = NetworkHelper.sendRequest("users.get", urlParameters);
            return json.getJSONArray("response").getJSONObject(0).getInt("id");
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
