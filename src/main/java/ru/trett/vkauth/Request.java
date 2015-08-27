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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class Request {

    public static String send(String targetUrl, String path, HashMap<String, String> urlParameters)
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

            if (exception instanceof NoHttpResponseException) {
                // Retry if the server dropped connection on us
                return true;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
                // Retry if the request is considered idempotent
                return false;
            }
            return false;
        };

        try (CloseableHttpClient httpclient = HttpClients.custom().setRetryHandler(myRetryHandler).build()) {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("https").setHost(targetUrl).setPath(path);
            if (urlParameters != null)
                urlParameters.forEach((key, value) -> uriBuilder.addParameter(key, value));
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



