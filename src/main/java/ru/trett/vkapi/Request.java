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

import java.util.HashMap;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Request {

    String host;
    String path;
    HashMap<String, String> query;

    Request() {
    }

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

