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

package ru.trett.vkauth;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Message {

    private String date;
    private String body;
    private String direction;
    private ArrayList<Attachment> attachments;

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void addAttachment(JSONObject attachment, boolean isLongPoll) {
        attachments = new ArrayList<>();
        Attachment a = new Attachment();
        if (isLongPoll) {
            a.setPhoto(attachment.getString("attach1_photo"));
            a.setTitle(attachment.getString("attach1_title"));
            a.setUrl(attachment.getString("attach1_url"));
            a.setDescription(attachment.getString("attach1_desc"));
            this.attachments.add(a);
        } else {
            switch (attachment.optString("type")) {
                case AttachmentType.LINK:
                    a.setUrl(attachment.getJSONObject("link").getString("url"));
                    a.setDescription(attachment.getJSONObject("link").getString("description"));
                    a.setTitle(attachment.getJSONObject("link").getString("title"));
                    break;
                case AttachmentType.GIFT:
                    a.setPhoto(attachment.getJSONObject("gift").getString("thumb_48"));
                    break;
                case AttachmentType.PHOTO:
                    //TODO: parse something
                    break;
                case AttachmentType.WALL:
                    //TODO: parse
                    break;
                case AttachmentType.STICKER:
                    a.setPhoto(attachment.getJSONObject("sticker").getString("photo_64"));
                    break;
            }
            this.attachments.add(a);
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public class Attachment {

        String photo = null;
        String title = null;
        String url = null;
        String description = null;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public final class AttachmentType {
        public static final String LINK = "link";
        public static final String STICKER = "sticker";
        public static final String PHOTO = "photo";
        public static final String WALL = "wall";
        public static final String GIFT = "gift";
    }

}


