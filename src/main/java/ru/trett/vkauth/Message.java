/*
 *  *
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

package ru.trett.vkauth;

public class Message {

    private String  date;
    private String body;
    private String direction;

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
}
