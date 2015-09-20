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

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;
import ru.trett.vkapi.Buddy;
import ru.trett.vkapi.Message;
import ru.trett.vkapi.VKUtils;

import java.io.FileReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author Roman Tretyakov
 * @since 20.09.15
 */
public class VKUtilsTest extends Assert {

    private JSONObject readFile(String fileName) {
        assertNotNull("Test file missing",
                getClass().getResource("/" + fileName));
        try {
            URL resourceUrl = getClass().
                    getResource("/" + fileName);
            Path resourcePath = Paths.get(resourceUrl.toURI());
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(resourcePath.toString()));
            return new JSONObject(obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void testUserMapNull() {
        VKUtils.userMapper(null);
    }

    @Test
    public void testUsersMapEmpty() {
        JSONObject json = readFile("empty.json");
        assert json != null;
        ArrayList<Buddy> buddies = VKUtils.userMapper(json.getJSONArray("items"));
        assertTrue(buddies.isEmpty());
    }

    @Test
    public void testUsersMapUsersGet() {
        JSONObject json = readFile("usersget.json");
        assert json != null;
        ArrayList<Buddy> buddies = VKUtils.userMapper(json.getJSONArray("response"));
        assertFalse(buddies.size() <= 0);
    }

    @Test
    public void testUsersMapFriendsGet() {
        JSONObject obj = readFile("friendsget.json");
        assert obj != null;
        JSONObject json = obj.getJSONObject("response");
        ArrayList<Buddy> buddies = VKUtils.userMapper(json.getJSONArray("items"));
        assertFalse(buddies.size() <= 0);
    }

    @Test
    public void testAnswerToMessagesNull() {
        VKUtils.answerToMessages(null);
    }

    @Test
    public void testAnswerToMessagesEmpty() {
        JSONObject json = readFile("emptyMessages.json");
        assert json != null;
        ArrayList<Message> messages = VKUtils.answerToMessages(json.getJSONObject("response"));
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testAnswerToMessagesMessagesGet() {
        JSONObject json = readFile("messagesHistory.json");
        assert json != null;
        ArrayList<Message> messages = VKUtils.answerToMessages(json.getJSONObject("response"));
        assertFalse(messages.size() <= 0);
    }

}
