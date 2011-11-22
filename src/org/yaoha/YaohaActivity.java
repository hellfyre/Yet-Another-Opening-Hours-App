package org.yaoha;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

public class YaohaActivity extends Activity implements OnClickListener {
	Button mapButton;
	Button startButton;
	private static final String[] SHOP_TYPES = new String[] {
        "groceries", "computer", "sport", "clothes", "gas station"
    };
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getBaseContext()); 
        if (prefs.getBoolean("start_with_map",false) == true) {
        	startActivity(new Intent(this, YaohaMapActivity.class));
		}
        mapButton = (Button) findViewById(R.id.fooButton);
        mapButton.setOnClickListener(this);
        startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(this);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, SHOP_TYPES);
        MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.searchTextfield);
        textView.setAdapter(adapter);
        textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
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
            case R.id.settings:
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
        if(v.getId() == R.id.fooButton) {
            startActivity(new Intent(this, YaohaMapActivity.class));
        }
    }
}
