package org.yaoha;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OSMSettingsActivity extends Activity implements OnClickListener{
    OAuthHelper helper = new OAuthHelper();
    private String OSM_TOKEN = "";
    private String OSM_SECRET_TOKEN = "";
    TextView osmtoken;
    TextView osmsecrettoken;
    TextView osmaccesstoken;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.osmsettings);
        Button setOSM = (Button) findViewById(R.id.button_set_osm);
        osmtoken = (TextView) findViewById(R.id.textView_osm_token_text);
        osmsecrettoken = (TextView) findViewById(R.id.textView_osm_secret_token_text);
        osmaccesstoken = (TextView) findViewById(R.id.textView_osm_access_token_text);
        try {
            setOSM.setOnClickListener(this);
        } catch (Exception e) {
            String bla = e.getMessage();
            Toast.makeText(this, bla, Toast.LENGTH_LONG);
        }
        
    }
    
    public void onClick(View v) {
        if(v.getId() == R.id.button_set_osm) {
            registerToOSM();
            //Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
        }
    }
    
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "onResume()", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, Teststring, Toast.LENGTH_LONG).show();
            }
            osmaccesstoken.setText(token[1]);
        }
        osmtoken.setText(OSM_TOKEN);
        osmsecrettoken.setText(OSM_SECRET_TOKEN);
    }
    
    public void registerToOSM(){
        try {
            String uri = helper.getRequestToken();
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(uri));
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); //TODO WHY YOU NO WORK?
            startActivity(intent);
            this.finish();
        } catch (Exception e) {
            e.getMessage();
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
