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

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class ApiConnector {
    DefaultHttpClient client;
    private static Header userAgentHeader;
    private static final String apiUrl = "api.openstreetmap.org";
    private static final String xapiUrl = "www.overpass-api.de";
    private static String oauthToken;
    private static String oauthTokenSecret;
    private static OAuthConsumer consumer;
    
    public ApiConnector() {
        String applicationVersion = "";
        Context ctx = YaohaActivity.getStaticApplicationContext();
        try {
            applicationVersion = ctx.getApplicationContext().getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            applicationVersion = "version_unset";
        }
        userAgentHeader = new BasicHeader("User-Agent", "YAOHA/" + applicationVersion + " (Android)");
        setConsumer();

        client = new DefaultHttpClient();
    }
    
    public HttpResponse getNodes(URI uri) throws ClientProtocolException, IOException {
        HttpGet request = new HttpGet(uri);
        request.setHeader(userAgentHeader);
        return client.execute(request);
    }
    
    public HttpResponse createNewChangeset() throws ClientProtocolException, IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
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
        consumer.sign(request);
        return client.execute(request);
    }
    
    public HttpResponse putNode(String changesetId, OsmNode node) throws ClientProtocolException, IOException, ParserConfigurationException, TransformerException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
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
        consumer.sign(request);
        return client.execute(request);
    }
    
    public HttpResponse closeChangeset(String changesetId) throws ClientProtocolException, IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        URI uri = null;
        try {
            uri = new URI("http", apiUrl, "/api/0.6/changeset/" + changesetId + "/close", null, null);
        } catch (URISyntaxException e) {
            Log.e(ApiConnector.class.getSimpleName(), "Closing changeset " + changesetId + " failed:");
            Log.e(ApiConnector.class.getSimpleName(), e.getMessage());
        }
        HttpPut request = new HttpPut(uri);
        request.setHeader(userAgentHeader);
        consumer.sign(request);
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
    
    public static boolean isAuthenticated() {
        if (consumer == null) setConsumer();
        return (consumer != null);
    }
    
    private static void setConsumer() {
        Context ctx = YaohaActivity.getStaticApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        oauthToken = oauthTokenSecret = null;
        consumer = null;
        oauthToken = preferences.getString(OAuth.OAUTH_TOKEN, null);
        oauthTokenSecret = preferences.getString(OAuth.OAUTH_TOKEN_SECRET, null);
        if (oauthToken != null && oauthTokenSecret != null) {
            consumer = new CommonsHttpOAuthConsumer(C.CONSUMER_KEY, C.CONSUMER_SECRET);
            consumer.setTokenWithSecret(oauthToken, oauthTokenSecret);
        }
    }

}
