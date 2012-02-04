package org.yaoha;

import java.net.URLEncoder;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;


public class RequestTokenActivity extends Activity {

	
	
    private OAuthConsumer consumer; 
    private OAuthProvider provider;
    private SharedPreferences prefs;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	try {
    		consumer = new CommonsHttpOAuthConsumer(C.CONSUMER_KEY, C.CONSUMER_SECRET);
    		provider = new CommonsHttpOAuthProvider(
    				C.REQUEST_URL  + "?scope=" + URLEncoder.encode(C.SCOPE, C.ENCODING) + "&xoauth_displayname=" + C.APP_NAME,
    				C.ACCESS_URL,
    				C.AUTHORIZE_URL);
    	} catch (Exception e) {
    		Log.e(C.TAG, "Error creating consumer / provider",e);
    	}

    	getRequestToken();
    }

	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent); 
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(C.OAUTH_CALLBACK_SCHEME)) {
			Log.i(C.TAG, "Callback received : " + uri);
			Log.i(C.TAG, "Retrieving Access Token");
			getAccessToken(uri);
		}
	}
	
	private void getRequestToken() {
		try {
			Log.d(C.TAG, "getRequestToken() called");
			String url = provider.retrieveRequestToken(consumer, C.OAUTH_CALLBACK_URL);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
			this.startActivity(intent);
			
		} catch (Exception e) {
			Log.e(C.TAG, "Error retrieving request token", e);
		}
	}
	
	private void getAccessToken(Uri uri) {
		final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
		try {
			provider.retrieveAccessToken(consumer, oauth_verifier);

			final Editor edit = prefs.edit();
			edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
			edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
			edit.commit();
			
			String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
			String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
			
			consumer.setTokenWithSecret(token, secret);
			this.startActivity(new Intent(this ,OSMSettingsActivity.class));

			Log.i(C.TAG, "Access Token Retrieved");
			
		} catch (Exception e) {
			Log.e(C.TAG, "Access Token Retrieval Error", e);
		}
	}
	
}
