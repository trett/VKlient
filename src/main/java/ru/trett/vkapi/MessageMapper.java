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
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */
public class MessageMapper {

    public ArrayList<Message> map(JSONObject object) {
        JSONArray array = object.getJSONArray("items");
        ArrayList<Message> messages = new ArrayList<>();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        for (int i = 0; i < array.length(); ++i) {
            Message m = new Message();
            date.setTime(array.getJSONObject(i).getLong("date") * 1000);
            if (array.getJSONObject(i).getInt("out") == 1) {
                m.setDirection("out");
            } else {
                m.setDirection("in");
            }
            m.setBody(array.getJSONObject(i).getString("body"));
            m.setDate(sdf.format(date));
            if (array.getJSONObject(i).has("attachments")) {
                JSONArray attachments = array.getJSONObject(i).getJSONArray("attachments");
                for (int j = 0; j < attachments.length(); ++j)
                    m.addAttachment(attachments.getJSONObject(j));
            }
            messages.add(m);
        }
        Collections.reverse(messages);
        return messages;
    }

}
