package org.yaoha;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TimePicker;

public class NodeEditAddHourRangeStarttimeActivity extends Activity implements OnClickListener  {
    TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_edit_add_hour_range_starttime);
        setTitle(R.string.node_edit_start_time);
        
        timePicker = (TimePicker) findViewById(R.id.nodeEditTimePickerStartTime);
        timePicker.setIs24HourView(true);
        if (getIntent().hasExtra("endTimeHour") && (getIntent().getIntExtra("endTimeHour", -1) > -1)) {
            int endTimeHour = getIntent().getIntExtra("endTimeHour", 0) - 1;
            if (endTimeHour < 0) endTimeHour = 0;
            int endTimeMinute = getIntent().getIntExtra("endTimeMinute", 0);
            timePicker.setCurrentHour(endTimeHour);
            timePicker.setCurrentMinute(endTimeMinute);
        }
        if (getIntent().hasExtra("startTimeHour")) {
            timePicker.setCurrentHour(getIntent().getIntExtra("startTimeHour", 0));
            timePicker.setCurrentMinute(getIntent().getIntExtra("startTimeMinute", 0));
        }
        else {
            timePicker.setCurrentHour(0);
            timePicker.setCurrentMinute(0);
        }
        
        initializeUi();
    }
    
    private void initializeUi() {
        Button buttonBackStarttime = (Button) findViewById(R.id.nodeEditButtonBackStarttime);
        buttonBackStarttime.setOnClickListener(this);
        Button buttonCancelStarttime = (Button) findViewById(R.id.nodeEditButtonCancelStarttime);
        buttonCancelStarttime.setOnClickListener(this);
        Button buttonNextStarttime = (Button) findViewById(R.id.nodeEditButtonNextStarttime);
        buttonNextStarttime.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.nodeEditButtonCancelStarttime:
            finish();
            break;
        case R.id.nodeEditButtonBackStarttime:
            Intent intentWeek = new Intent();
            intentWeek.putExtra("direction", NodeEditActivity.DIRECTION_TO_WEEK);
            putStarttimeIntoIntent(intentWeek);
            copyIntentContents(intentWeek);
            setResult(RESULT_OK, intentWeek);
            finish();
            break;
        case R.id.nodeEditButtonNextStarttime:
            Intent intentEnd = new Intent();
            intentEnd.putExtra("direction", NodeEditActivity.DIRECTION_TO_END);
            putStarttimeIntoIntent(intentEnd);
            copyIntentContents(intentEnd);
            setResult(RESULT_OK, intentEnd);
            finish();
            break;
        default:
            break;
        }
    }
    
    private void putStarttimeIntoIntent(Intent intent) {
        intent.putExtra("startTimeHour", timePicker.getCurrentHour());
        intent.putExtra("startTimeMinute", timePicker.getCurrentMinute());
    }
    
    private void copyIntentContents(Intent intent) {
        for (int day=OpeningHours.MONDAY; day <= OpeningHours.SUNDAY; day++) {
            if (getIntent().hasExtra(OpeningHours.weekDayToString(day))) {
                intent.putExtra(OpeningHours.weekDayToString(day), getIntent().getBooleanExtra(OpeningHours.weekDayToString(day), false));
            }
        }
        if (getIntent().hasExtra("endTimeHour")) {
            intent.putExtra("endTimeHour", getIntent().getIntExtra("endTimeHour", -2));
            intent.putExtra("endTimeMinute", getIntent().getIntExtra("endTimeMinute", -2));
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int[] time = { timePicker.getCurrentHour(), timePicker.getCurrentMinute() };
        outState.putIntArray("starttime", time);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("starttime")) {
            int[] time = savedInstanceState.getIntArray("starttime");
            timePicker.setCurrentHour(time[0]);
            timePicker.setCurrentMinute(time[1]);
        }
    }

}
