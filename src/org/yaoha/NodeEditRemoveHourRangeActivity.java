package org.yaoha;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NodeEditRemoveHourRangeActivity extends Activity implements OnClickListener {
    private String title;
    private String message;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_edit_remove_hour_range);
        
        this.title = "Remove " + getIntent().getStringExtra("hourrange");
        this.message = "Remove range " +
                    getIntent().getStringExtra("hourrange") +
                    " from " +
                    OpeningHours.weekDayToString(getIntent().getIntExtra("day", -1)) +
                    " or from all days?";
        
        initializeUi();
    }

    private void initializeUi() {
        TextView titleView = (TextView) findViewById(R.id.nodeEditTextViewRemoveTitle);
        titleView.setText(title);
        TextView messageView = (TextView) findViewById(R.id.nodeEditTextViewRemoveMessage);
        messageView.setText(message);
        Button cancelButton = (Button) findViewById(R.id.nodeEditButtonCancelRemove);
        cancelButton.setOnClickListener(this);
        Button thisButton = (Button) findViewById(R.id.nodeEditButtonRemoveThis);
        thisButton.setOnClickListener(this);
        Button allButton = (Button) findViewById(R.id.nodeEditButtonRemoveAll);
        allButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.putExtra("hourrange", getIntent().getStringExtra("hourrange"));
        intent.putExtra("day", getIntent().getIntExtra("day", -1));
        switch (v.getId()) {
        case R.id.nodeEditButtonCancelRemove:
            finish();
            break;
        case R.id.nodeEditButtonRemoveThis:
            intent.putExtra("removeall", false);
            setResult(RESULT_OK, intent);
            finish();
            break;
        case R.id.nodeEditButtonRemoveAll:
            intent.putExtra("removeall", true);
            setResult(RESULT_OK, intent);
            finish();
            break;
        default:
            break;
        }
    }

}
