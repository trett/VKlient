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

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.trett.vkapi.*;

import java.io.FileReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author Roman Tretyakov
 * @since 20.09.15
 */
public class TestAPI extends Assert {

    private final Message m = new Message();

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
    public void testUsersMapEmpty() {
        JSONObject json = readFile("empty.json");
        assert json != null;
        ArrayList<Buddy> buddies = new BuddyMapper().map(json.getJSONArray("items"));
        assertTrue(buddies.isEmpty());
    }

    @Test
    public void testUsersMapUsersGet() {
        JSONObject json = readFile("usersget.json");
        assert json != null;
        ArrayList<Buddy> buddies = new BuddyMapper().map(json.getJSONArray("response"));
        assertFalse(buddies.size() <= 0);
    }

    @Test
    public void testUsersMapFriendsGet() {
        JSONObject obj = readFile("friendsget.json");
        assert obj != null;
        JSONObject json = obj.getJSONObject("response");
        ArrayList<Buddy> buddies = new BuddyMapper().map(json.getJSONArray("items"));
        assertFalse(buddies.size() <= 0);
    }

    @Test
    public void testAnswerToMessagesEmpty() {
        JSONObject json = readFile("emptyMessages.json");
        assert json != null;
        ArrayList<Message> messages = new MessageMapper().map(json.getJSONObject("response"));
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testAnswerToMessagesMessagesGet() {
        JSONObject json = readFile("messagesHistory.json");
        assert json != null;
        ArrayList<Message> messages = new MessageMapper().map(json.getJSONObject("response"));
        assertFalse(messages.size() <= 0);
    }

    @Before
    public void testAddEmptyAttachment() {
        m.addAttachment(new JSONObject("{attachments:[]}"));
        JSONObject json = readFile("attachments.json");
        assert json != null;
        JSONArray array = json.getJSONArray("attachments");
        for (int i = 0; i < array.length(); ++i) {
            m.addAttachment(array.getJSONObject(i));
        }
    }

    @Test
    public void testGetAttachments() {
        assertTrue(m.getAttachments().size() > 0);
        assertEquals(m.getAttachments().get(0).getPhoto(), "http://vk.com/images/stickers/50/64b.png");
    }

}
