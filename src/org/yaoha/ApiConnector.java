package org.yaoha;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class ApiConnector {
    HttpClient client;
    private static final String apiUrl = "api.openstreetmap.org";
    private static final String devApiIp = "192.168.178.115";
    private static final int devApiPort = 3000;
    private static final String xapiUrl = "www.overpass-api.de";
    
    public ApiConnector() {
        client = new DefaultHttpClient();
    }
    
    public InputStream getNodes(URI uri) throws ClientProtocolException, IOException {
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        InputStream in = response.getEntity().getContent();
        return in;
    }
    
    public InputStream putNodes(URI uri) {
        InputStream in = null;
        return in;
    }
    
    public static URI getRequestUriXapi(String longitudeLow, String latitudeLow, String longitudeHigh, String latitudeHigh, String name, String amenity, String shop) {
        String requestString = "node[bbox=" + longitudeLow + "," + latitudeLow + "," + longitudeHigh + "," + latitudeHigh + "]";
        if (name != null) requestString += "[name=*" + name + "*]";
        if (amenity != null) requestString += "[amenity=*" + amenity + "*]";
        if (shop != null) requestString += "[shop=*" + shop + "*]";
        
        //TODO: remove this.
        requestString += "[opening_hours=*]";
        
        URI uri = null;
        try {
            uri = new URI("http", xapiUrl, "/api/xapi", requestString, null);
        } catch (URISyntaxException e) {
            Log.d(ApiConnector.class.getSimpleName(), e.getMessage());
        }
        
        return uri;
    }
    
    public static URI getRequestUriApiGetNode(String id) {
        URI uri = null;
        try {
            uri = new URI("http", apiUrl, "/api/0.6/node/" + id, null);
        } catch (URISyntaxException e) {
            Log.d(ApiConnector.class.getSimpleName(), e.getMessage());
        }
        
        return uri;
    }
    
    public static URI getRequestUriDevApiGetNode(String id) {
        URI uri = null;
        try {
            uri = new URI("http", null, devApiIp, devApiPort, "/api/0.6/node/" + id, null, null);
        } catch (URISyntaxException e) {
            Log.d(ApiConnector.class.getSimpleName(), e.getMessage());
        }
        
        return uri;
    }
    
    public static URI getRequestUriApiUpdateNode(String id) {
        return null;
    }

}
