/*
 *  *
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

package ru.trett.vkauth;

import java.util.HashMap;

public class Request {

    String host;
    String path;
    HashMap<String, String> query;

    Request() {}

    Request(String host, String path, HashMap<String, String> query) {
        this.host = host;
        this.path = path;
        this.query = query;
    }

}

class RequestBuilder {

    private String _host;
    private String _path = "";
    private HashMap<String, String> _query;
    private int _timeout = 5000;

    public RequestBuilder() {
    }

    public Request build() {
        return new Request(_host, _path, _query);
    }

    public RequestBuilder host(String _host) {
        this._host = _host;
        return this;
    }

    public RequestBuilder path(String _path) {
        this._path = _path;
        return this;
    }

    public RequestBuilder query(HashMap<String, String> _query) {
        this._query = _query;
        return this;
    }

}

