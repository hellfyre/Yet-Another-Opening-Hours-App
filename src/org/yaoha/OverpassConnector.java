package org.yaoha;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class OverpassConnector {
    HttpClient client;

    public OverpassConnector() {
        client = new DefaultHttpClient();
    }

    private HttpResponse getRequest(URI uri) {
        HttpGet request = new HttpGet(uri);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return response;
    }

    public HttpResponse getRequestResponse(String requestString) {
        URI uri = null;
        try {
            uri = new URI("http", "www.overpass-api.de", "/api/xapi",
                    requestString, null);
        } catch (URISyntaxException e) {
            // This shouldn't happen, since the uri is created by program code
            // rather than by the user
            e.printStackTrace();
        }
        return getRequest(uri);
    }

    public String getRequestString(String requestString) {
        HttpResponse response = getRequestResponse(requestString);
        BufferedReader in = null;
        String responseString = "";
        try {
            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            String line;
            while ((line = in.readLine()) != null) {
                responseString += line + "\n";
            }
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return responseString;
    }

    public InputStream getResponseInputStream(String requestString) {
        HttpResponse response = getRequestResponse(requestString);
        InputStream in = null;
        try {
            in = response.getEntity().getContent();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return in;
    }

}
