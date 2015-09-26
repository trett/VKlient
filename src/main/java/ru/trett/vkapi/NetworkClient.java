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

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class NetworkClient {

    private static final PoolingHttpClientConnectionManager connPool;

    static {

        connPool = new PoolingHttpClientConnectionManager();
        connPool.setMaxTotal(10);
        connPool.setDefaultMaxPerRoute(5);

    }

    private final CloseableHttpClient httpClient;
    private final ResponseHandler<String> responseHandler = new BasicResponseHandler();
    private HttpPost httpPost;
    private CloseableHttpResponse httpResponse;

    NetworkClient(int timeout) {
        HttpRequestRetryHandler customRetryHandler = (exception, executionCount, context) -> {
            if (executionCount >= 5) {
                // Do not retry if over max retry count
                return false;
            }

            if (exception instanceof SSLException) {
                // SSL handshake exception
                return true;
            }

            if (exception instanceof ConnectTimeoutException) {
                // Connection refused
                return true;
            }
            if (exception instanceof SocketTimeoutException) {
                return true;
            }

            if (exception instanceof NoHttpResponseException) {
                // Retry if the server dropped connection on us
                return true;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest httpRequest = clientContext.getRequest();
            boolean idempotent = !(httpRequest instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
                // Retry if the request is considered idempotent
                return false;
            }
            return false;
        };

        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setCookieSpec(CookieSpecs.STANDARD).
                        build();

        httpClient = HttpClients
                .custom()
                .setRetryHandler(customRetryHandler)
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connPool)
                .setConnectionManagerShared(true)
                .build();

    }

    /**
     * Send Post Request to https
     *
     * @param request Request
     * @return String answer
     * @throws ClientProtocolException
     */
    public String send(Request request)
            throws ClientProtocolException {
        try {
            connPool.closeExpiredConnections();
            httpPost = new HttpPost(
                    new URIBuilder()
                            .setScheme("https")
                            .setHost(request.host)
                            .setPath(request.path)
                            .build()
            );
            List<NameValuePair> list = new ArrayList<>();
            if (request.query != null)
                request.query.forEach((key, value) -> list.add(new BasicNameValuePair(key, value)));
            httpPost.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            System.out.println("Executing request " + httpPost.getRequestLine());
            httpResponse = httpClient.execute(httpPost);
            String responseBody = responseHandler.handleResponse(httpResponse);
            System.out.println(responseBody);
            return responseBody;
        } catch (IOException | URISyntaxException e) {
            System.out.println(e.getMessage());
            return null;
        } finally {
            try {
                if (httpResponse != null)
                    httpResponse.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void abort() {
        httpPost.abort();
    }

}





