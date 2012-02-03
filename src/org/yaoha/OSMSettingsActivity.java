package org.yaoha;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class OSMSettingsActivity extends Activity {
    OAuthHelper helper = new OAuthHelper();
    private String OSM_TOKEN = "";
    private String OSM_SECRET_TOKEN = "";
    
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        onResume();
//        super.onCreate(savedInstanceState);
//    }
    
    
    protected void onResume() {
        super.onResume();
        
        String[] token = getVerifier();
        if (token != null) {
            try {
                String accessToken[] = helper.getAccessToken(token[1]);
                this.OSM_TOKEN = accessToken[0];
                YaohaActivity.setToken(accessToken[0]);
                this.OSM_SECRET_TOKEN = accessToken[1];
                YaohaActivity.setSecretToken(accessToken[1]);
            } catch (Exception e) {
                String Teststring = e.getMessage();
                String abc = Teststring;
            }
            
        }
    }
    
    private String[] getVerifier() {
        // extract the token if it exists
        Uri uri = this.getIntent().getData();
        if (uri == null) {
            return null;
        }

        String token = uri.getQueryParameter("oauth_token");
        String verifier = uri.getQueryParameter("oauth_verifier");
        return new String[] { token, verifier };
    }
}
