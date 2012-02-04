package org.yaoha;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class ApiConnector {
    DefaultHttpClient client;
    private static final String apiUrl = "home.uschok.de";
    private static final String xapiUrl = "www.overpass-api.de";
    private static String username = "foobar";
    private static String password = "foobarbaz";
    
    public ApiConnector() {
        // This is, what makes the DefaultHttpClient choose basic auth over digest auth
        // TODO: Remove Interceptor soon as OAuth works
        HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
            public void process(final org.apache.http.HttpRequest request, final HttpContext context) throws HttpException, IOException {
                AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                        ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                
                if (authState.getAuthScheme() == null) {
                    AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                    Credentials creds = credsProvider.getCredentials(authScope);
                    if (creds != null) {
                        authState.setAuthScheme(new BasicScheme());
                        authState.setCredentials(creds);
                    }
                }
            }
        };
        client = new DefaultHttpClient();
        // TODO: Remove the following two lines soon as OAuth works
        client.addRequestInterceptor(preemptiveAuth, 0);
        client.getCredentialsProvider().setCredentials(new AuthScope(apiUrl, 80), new UsernamePasswordCredentials(username, password));
    }
    
    public InputStream getNodes(URI uri) throws ClientProtocolException, IOException {
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        InputStream in = response.getEntity().getContent();
        return in;
    }
    
    public InputStream createNewChangeset() throws ClientProtocolException, IOException {
        URI uri = null;
        try {
            uri = new URI("http", apiUrl, "/api/0.6/changeset/create", null, null);
        } catch (URISyntaxException e) {
            Log.e(ApiConnector.class.getSimpleName(), "Creating new changeset failed:");
            Log.e(ApiConnector.class.getSimpleName(), e.getMessage());
        }
        HttpPut request = new HttpPut(uri);
        String requestString = "<osm>"
                + "<changeset>"
                + "<tag k=\"created_by\" v=\"YAOHA\"/>"
                + "<tag k=\"comment\" v=\"Updating opening hours\"/>"
                + "</changeset>"
                + "</osm>";
        HttpEntity entity = new StringEntity(requestString);
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        return response.getEntity().getContent();
    }
    
    public InputStream putNode(String changesetId, OsmNode node) throws ClientProtocolException, IOException, ParserConfigurationException, TransformerException {
        URI uri = null;
        try {
            uri = new URI("http", apiUrl, "/api/0.6/node/" + String.valueOf(node.getID()), null);
        } catch (URISyntaxException e) {
            Log.e(ApiConnector.class.getSimpleName(), "Uploading node " + String.valueOf(node.getID()) + " failed:");
            Log.e(ApiConnector.class.getSimpleName(), e.getMessage());
        } 
        HttpPut request = new HttpPut(uri);
        String requestString = "";
        requestString = node.serialize(changesetId);
        HttpEntity entity = new StringEntity(requestString);
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        return response.getEntity().getContent();
    }
    
    public InputStream closeChangeset(String changesetId) throws ClientProtocolException, IOException {
        URI uri = null;
        try {
            uri = new URI("http", apiUrl, "/api/0.6/changeset/" + changesetId + "/close", null, null);
        } catch (URISyntaxException e) {
            Log.e(ApiConnector.class.getSimpleName(), "Closing changeset " + changesetId + " failed:");
            Log.e(ApiConnector.class.getSimpleName(), e.getMessage());
        }
        HttpPut request = new HttpPut(uri);
        HttpResponse response = client.execute(request);
        return response.getEntity().getContent();
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

}
