package org.yaoha;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import oauth.signpost.http.HttpRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class ApiConnector {
    HttpClient client;
    private static final String apiUrl = "api.openstreetmap.org";
    private static final String devApiIp = "134.169.35.227";
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
    
    public InputStream createNewChangeset() throws ClientProtocolException, IOException {
        URI uri = null;
        try {
            uri = new URI("http", null, devApiIp, devApiPort, "/api/0.6/changeset/create", null, null);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpPut request = new HttpPut(uri);
        String requestString = "<osm>" +
        		"<changeset>" +
        		"<tag k=\"created_by\" v=\"YAOHA\"/>" +
        		"<tag k=\"comment\" v=\"Updating opening hours\"/>" +
        		"</changeset>" +
        		"</osm>";
        HttpEntity entity = new StringEntity(requestString);
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        return response.getEntity().getContent();
    }
    
    public InputStream uploadNode(URI uri, String changesetId, OsmNode node) throws ClientProtocolException, IOException {
        HttpPut request = new HttpPut(uri);
        String requestString = "<osm>";
        try {
            requestString += node.serialize(changesetId);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        requestString += "</osm>";
        HttpEntity entity = new StringEntity(requestString);
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        return response.getEntity().getContent();
    }
    
    public void closeChangeset(String changesetId) throws ClientProtocolException, IOException {
        URI uri = null;
        try {
            uri = new URI("http", null, devApiIp, devApiPort, "/api/0.6/changeset/" + changesetId + "/close", null, null);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpPut request = new HttpPut(uri);
        client.execute(request);
    }
    
    public static List<URI> getRequestUriXapi(String longitudeLow, String latitudeLow, String longitudeHigh, String latitudeHigh, String name, String amenity, String shop, boolean edit_mode) {
        String requestString = "node[bbox=" + longitudeLow + "," + latitudeLow + "," + longitudeHigh + "," + latitudeHigh + "]";
        if (name != null)
            requestString += "[name=*" + name + "*]";
        else
            requestString += "[name=*]";
        
        if (!edit_mode)
            requestString += "[opening_hours=*]";
        
        String requestStringAmenity = requestString;;
        if (amenity != null)
            requestStringAmenity += "[amenity=*" + amenity + "*]";
        else
            requestStringAmenity += "[amenity=*]";
        String requestStringShop = requestString;
        if (shop != null)
            requestStringShop += "[shop=*" + shop + "*]";
        else
            requestStringShop += "[shop=*]";
        
        List<URI> requestStrings = new ArrayList<URI>();
        try {
            URI uri = new URI("http", xapiUrl, "/api/xapi", requestStringAmenity, null);
            requestStrings.add(uri);
        } catch (URISyntaxException e) {
            Log.d(ApiConnector.class.getSimpleName(), e.getMessage());
        }

        try {
            URI uri = new URI("http", xapiUrl, "/api/xapi", requestStringShop, null);
            requestStrings.add(uri);
        } catch (URISyntaxException e) {
            Log.d(ApiConnector.class.getSimpleName(), e.getMessage());
        }
        
        return requestStrings;
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
    
    public static URI getRequestUriDevApiUpdateNode(String nodeId) {
        URI uri = null;
        try {
            uri = new URI("http", null, devApiIp, devApiPort, "/api/0.6/node/" + nodeId, null, null);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return uri;
    }

}
