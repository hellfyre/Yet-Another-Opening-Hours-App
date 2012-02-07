/*
 *  This file is part of YAOHA.
 *
 *  YAOHA is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YAOHA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YAOHA.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright 2012 Stefan Hobohm, Lutz Reinhardt, Matthias Uschok
 *
 */

package org.yaoha;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.http.Header;
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
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class ApiConnector {
    DefaultHttpClient client;
    private static final Header userAgentHeader = new BasicHeader("User-Agent", "YAOHA/0.1 (Android)");
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
    
    public HttpResponse getNodes(URI uri) throws ClientProtocolException, IOException {
        HttpGet request = new HttpGet(uri);
        request.setHeader(userAgentHeader);
        return client.execute(request);
    }
    
    public HttpResponse createNewChangeset() throws ClientProtocolException, IOException {
        URI uri = null;
        try {
            uri = new URI("http", apiUrl, "/api/0.6/changeset/create", null, null);
        } catch (URISyntaxException e) {
            Log.e(ApiConnector.class.getSimpleName(), "Creating new changeset failed:");
            Log.e(ApiConnector.class.getSimpleName(), e.getMessage());
        }
        HttpPut request = new HttpPut(uri);
        request.setHeader(userAgentHeader);
        String requestString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<osm>"
                + "<changeset>"
                + "<tag k=\"created_by\" v=\"YAOHA\"/>"
                + "<tag k=\"comment\" v=\"Updating opening hours\"/>"
                + "</changeset>"
                + "</osm>";
        HttpEntity entity = new StringEntity(requestString, HTTP.UTF_8);
        request.setEntity(entity);
        return client.execute(request);
    }
    
    public HttpResponse putNode(String changesetId, OsmNode node) throws ClientProtocolException, IOException, ParserConfigurationException, TransformerException {
        URI uri = null;
        try {
            uri = new URI("http", apiUrl, "/api/0.6/node/" + String.valueOf(node.getID()), null);
        } catch (URISyntaxException e) {
            Log.e(ApiConnector.class.getSimpleName(), "Uploading node " + String.valueOf(node.getID()) + " failed:");
            Log.e(ApiConnector.class.getSimpleName(), e.getMessage());
        } 
        HttpPut request = new HttpPut(uri);
        request.setHeader(userAgentHeader);
        String requestString = "";
        requestString = node.serialize(changesetId);
        HttpEntity entity = new StringEntity(requestString, HTTP.UTF_8);
        request.setEntity(entity);
        return client.execute(request);
    }
    
    public HttpResponse closeChangeset(String changesetId) throws ClientProtocolException, IOException {
        URI uri = null;
        try {
            uri = new URI("http", apiUrl, "/api/0.6/changeset/" + changesetId + "/close", null, null);
        } catch (URISyntaxException e) {
            Log.e(ApiConnector.class.getSimpleName(), "Closing changeset " + changesetId + " failed:");
            Log.e(ApiConnector.class.getSimpleName(), e.getMessage());
        }
        HttpPut request = new HttpPut(uri);
        request.setHeader(userAgentHeader);
        return client.execute(request);
    }
    
    public static List<URI> getRequestUriXapi(String longitudeLow, String latitudeLow, String longitudeHigh, String latitudeHigh, String name, String amenity, String shop, boolean edit_mode) {
        String requestString = "node[bbox=" + longitudeLow + "," + latitudeLow + "," + longitudeHigh + "," + latitudeHigh + "]";
        if (name != null)
            requestString += "[name=*" + name + "*]";
        else
            requestString += "[name=*]";
        
        if (!edit_mode)
            requestString += "[opening_hours=*]";
        
        String requestStringAmenity = requestString;
        String requestStringShop = requestString;
        if (edit_mode) {
            if (amenity != null)
                requestStringAmenity += "[amenity=*" + amenity + "*]";
            else
                requestStringAmenity += "[amenity=*]";
            if (shop != null)
                requestStringShop += "[shop=*" + shop + "*]";
            else
                requestStringShop += "[shop=*]";
        }
        
        List<URI> requestStrings = new ArrayList<URI>();
        try {
            URI uri = new URI("http", xapiUrl, "/api/xapi", requestStringAmenity, null);
            requestStrings.add(uri);
        } catch (URISyntaxException e) {
            Log.d(ApiConnector.class.getSimpleName(), e.getMessage());
        }

        if (!requestStringAmenity.contentEquals(requestStringShop)) {
            try {
                URI uri = new URI("http", xapiUrl, "/api/xapi", requestStringShop, null);
                requestStrings.add(uri);
            } catch (URISyntaxException e) {
                Log.d(ApiConnector.class.getSimpleName(), e.getMessage());
            }
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
