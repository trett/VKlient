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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import ru.trett.vkapi.Exceptions.RequestReturnErrorException;
import ru.trett.vkapi.Exceptions.RequestReturnNullException;

import java.util.Map;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */
public class NetworkHelper {

    private static final String API_VERSION = "5.37";
    private static NetworkClient networkClient;

    static {
        networkClient = new NetworkClient(5000);
    }


    public static JSONObject sendRequest(final String vkMethod,
                                         final Map<String, String> urlParameters)
            throws RequestReturnNullException, RequestReturnErrorException {
        try {
            urlParameters.put("v", API_VERSION);
            Request request = new RequestBuilder()
                    .host("api.vk.com/method/")
                    .path(vkMethod)
                    .query(urlParameters)
                    .build();
            String str = networkClient.send(request);
            if (str == null)
                throw new RequestReturnNullException("NetworkClient return null");
            JSONObject receivedAnswer = new JSONObject(str);
            if (receivedAnswer.has("error")) {
                throw new RequestReturnErrorException(
                        "NetworkClient return error: " + receivedAnswer.getJSONObject("error").getString("error_msg"));
            }
            System.out.println(urlParameters); //debug output
            return receivedAnswer;
        } catch (ClientProtocolException e) {
            throw new RequestReturnNullException("NetworkClient return null.", e);
        }
    }

    public static void close() {
        networkClient.abort();
    }

}
