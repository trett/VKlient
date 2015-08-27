package ru.trett.vkauth;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

public class NetworkClient {

    //    public static String send(String targetUrl, String path, HashMap<String, String> urlParameters)
    public static String send(Request request)
            throws ClientProtocolException {
        HttpRequestRetryHandler myRetryHandler = (exception, executionCount, context) -> {
            if (executionCount >= 5) {
                // Do not retry if over max retry count
                return false;
            }

            if (exception instanceof SSLException) {
                // SSL handshake exception
                System.out.println("SSLException");
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

        RequestConfig requestConfig = RequestConfig.custom().
                setConnectionRequestTimeout(request.timeout).
                setSocketTimeout(request.timeout).
                build();
        try (CloseableHttpClient httpclient = HttpClients.custom().
                setRetryHandler(myRetryHandler).
                setDefaultRequestConfig(requestConfig).
                build()) {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("https").setHost(request.host).
                    setPath(request.path);
            if (request.query != null)
                request.query.forEach((key, value) -> uriBuilder.addParameter(key, value));
            URI uri = uriBuilder.build();
            HttpGet httpget = new HttpGet(uri);
            System.out.println("Executing request " + httpget.getRequestLine());

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println(responseBody);
            httpclient.close();
            return responseBody;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

}





