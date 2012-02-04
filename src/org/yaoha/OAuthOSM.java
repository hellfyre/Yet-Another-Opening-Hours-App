package org.yaoha;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

public class OAuthOSM {





    private static final String PASSWORD = null;
    private static final String USERNAME = null;

    public static void main(String[] args) throws Exception {

        OAuthConsumer consumer = new DefaultOAuthConsumer(USERNAME, PASSWORD);

//        String scope = "http://www.blogger.com/feeds";
        OAuthProvider provider = new DefaultOAuthProvider(
                "http://www.openstreetmap.org/oauth/request_token"/*+ URLEncoder.encode(scope, "utf-8")*/,
                "http://www.openstreetmap.org/oauth/access_token",
                "http://www.openstreetmap.org/oauth/authorize");

        Toast.makeText(this, "Fetching request token...", Toast.LENGTH_SHORT).show();

        String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);

        System.out.println("Request token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());
        
        

        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(authUrl));
        startActivity(viewIntent);
        
//        System.out.println("Now visit:\n" + authUrl + "\n... and grant this app authorization");
//        System.out.println("Enter the verification code and hit ENTER when you're done:");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String verificationCode = br.readLine();

        System.out.println("Fetching access token...");

        provider.retrieveAccessToken(consumer, verificationCode.trim());

        System.out.println("Access token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        URL url = new URL("http://openstreetmap.de/karte.html");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();

        consumer.sign(request);

        System.out.println("Sending request...");
        request.connect();

        System.out.println("Response: " + request.getResponseCode() + " " + request.getResponseMessage());
    }
}
