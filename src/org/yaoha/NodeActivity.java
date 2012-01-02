package org.yaoha;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.yaoha.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;

public class NodeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nodeview);
        Intent i = getIntent();
        Bundle b = i.getExtras();
        
        LinearLayout ll = (LinearLayout) findViewById(R.id.list);
        
        List<String> nodeElements = new LinkedList<String>();
        Set<String> keySet = b.keySet();
        for (String key : keySet) {
            nodeElements.add(key);
            TextView name = new TextView(getApplicationContext());
            name.setText(key);
            ll.addView(name);
            EditText value = new EditText(getApplicationContext());
            value.setText(b.getString(key));
            ll.addView(value);
        }
    }
}
