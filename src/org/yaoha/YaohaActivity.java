package org.yaoha;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class YaohaActivity extends Activity implements OnClickListener {
	Button mapButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.osm_account:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.quit_app:
                this.finish();
                return true;
            case R.id.buy_pro:
                Toast.makeText(this, "You just payed 49,99€. Enjoy this Pro-Version!", Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

	@Override
	public void onClick(View v) {
		  startActivity(new Intent(this, YaohaMapActivity.class));
	}
}
