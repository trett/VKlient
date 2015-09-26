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
import ru.trett.vkapi.Exceptions.TokenErrorException;

import java.util.*;
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
     * @param token   String access_token
     * @return ArrayList buddies
     */
    public static ArrayList<Buddy> get(List<Integer> userIds, String token) {
        Map<String, String> urlParameters = new WeakHashMap<>();
        String ids = userIds.stream().map(Object::toString).collect(Collectors.joining(","));
        urlParameters.put("user_ids", ids);
        urlParameters.put("access_token", token);
        urlParameters.put("fields", "photo_50,online,status");
        try {
            JSONObject obj = NetworkHelper.sendRequest("users.get", urlParameters);
            return new BuddyMapper().map(obj.getJSONArray("response"));
        } catch (RequestReturnNullException | RequestReturnErrorException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return user_id for given token
     *
     * @param token String access_token
     * @return int user_id
     */
    public static int get(String token) throws TokenErrorException, RequestReturnNullException {
        Map<String, String> urlParameters = new WeakHashMap<>();
        urlParameters.put("access_token", token);
        try {
            JSONObject json = NetworkHelper.sendRequest("users.get", urlParameters);
            return json.getJSONArray("response").getJSONObject(0).getInt("id");
        } catch (RequestReturnErrorException e) {
            throw new TokenErrorException("Token error");
        }
    }

}
