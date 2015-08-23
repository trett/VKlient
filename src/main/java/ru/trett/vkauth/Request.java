package ru.trett.vkauth;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class Request {

    public static String send(String targetUrl, String path, HashMap<String, String> urlParameters) {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("https").setHost(targetUrl).setPath(path);
            urlParameters.forEach((key, value) -> uriBuilder.addParameter(key, value));
            URI uri = uriBuilder.build();
            HttpGet httpget = new HttpGet(uri);

            System.out.println("Executing request " + httpget.getRequestLine());

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
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



