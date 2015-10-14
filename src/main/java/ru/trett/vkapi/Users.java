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

import ru.trett.vkapi.Exceptions.RequestReturnErrorException;
import ru.trett.vkapi.Exceptions.RequestReturnNullException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
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
    public ArrayList<Buddy> get(List<Integer> userIds, String token)
            throws RequestReturnNullException, RequestReturnErrorException {
        Map<String, String> urlParameters = new WeakHashMap<>();
        String ids = userIds.stream().map(Object::toString).collect(Collectors.joining(","));
        urlParameters.put("user_ids", ids);
        urlParameters.put("access_token", token);
        urlParameters.put("fields", "photo_50,online,status");
        return new BuddyMapper()
                .map(new NetworkHelper()
                        .sendRequest("users.get", urlParameters)
                        .getJSONArray("response"));
    }

}
