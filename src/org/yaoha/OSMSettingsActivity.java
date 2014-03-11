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

import oauth.signpost.OAuth;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class OSMSettingsActivity extends Activity implements android.view.View.OnClickListener{
    private SharedPreferences prefs;
    TextView console, token, sToken, aToken;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.osmsettings);

        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        console = (TextView) findViewById(R.id.text_console);
        
        token = (TextView) findViewById(R.id.textView_osm_token_text);
        sToken = (TextView) findViewById(R.id.textView_osm_secret_token_text);
        //aToken = (TextView) findViewById(R.id.textView_osm_access_token_text);
        
        
        Button launchOauth = (Button) findViewById(R.id.button_set_osm);
        Button clearCredentials = (Button) findViewById(R.id.button_remove_account);
//        Button getContacts = (Button) findViewById(R.id.button_get_contacts);
        
        launchOauth.setOnClickListener(this);
        clearCredentials.setOnClickListener(this);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (isOAuthSuccessful()) {
            // OAuth successful, try getting the contacts
            console.setText("OAuth successful!");
        }
        else {
            console.setText("OAuth failed, no tokens, Click on the \"Set OSM\" Button.");
        }
    }
    

    private void clearCredentials() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final Editor edit = prefs.edit();
        edit.remove(OAuth.OAUTH_TOKEN);
        edit.remove(OAuth.OAUTH_TOKEN_SECRET);
        edit.commit();
    }
    
    private boolean isOAuthSuccessful() {
        String token = prefs.getString(OAuth.OAUTH_TOKEN, null);
        String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, null);
        if (token != null && secret != null){
            this.token.setText(token);
            this.sToken.setText(secret);
            return true;
        } else {
            return false;
        }
    }

    
/*    private OAuthConsumer getConsumer(SharedPreferences prefs) {
        String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
        String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(C.CONSUMER_KEY, C.CONSUMER_SECRET);
        consumer.setTokenWithSecret(token, secret);
        return consumer;
    }*/

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_set_osm) {
            startActivity(new Intent().setClass(v.getContext(), RequestTokenActivity.class));
            this.finish();
        }
        if(v.getId() == R.id.button_remove_account){
            clearCredentials();
            console.setText("No tokens found, no account set.");
        }
        
    }
}
