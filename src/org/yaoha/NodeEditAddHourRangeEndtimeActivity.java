package org.yaoha;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TimePicker;

public class NodeEditAddHourRangeEndtimeActivity extends Activity implements OnClickListener, OnCheckedChangeListener  {
    TimePicker timePicker;
    CheckBox openEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_edit_add_hour_range_endtime);
        setTitle(R.string.node_edit_end_time);
        
        timePicker = (TimePicker) findViewById(R.id.nodeEditTimePickerEndTime);
        timePicker.setIs24HourView(true);
        openEnd = (CheckBox) findViewById(R.id.nodeEditCheckBoxOpenEnd);
        openEnd.setOnCheckedChangeListener(this);
        if (getIntent().hasExtra("endTimeHour")) {
            int endTimeHour = getIntent().getIntExtra("endTimeHour", 0);
            int endTimeMinute = getIntent().getIntExtra("endTimeMinute", 0);
            if (endTimeHour == -1 && endTimeMinute == -1) {
                timePicker.setEnabled(false);
                openEnd.setChecked(true);
            }
            else {
                timePicker.setCurrentHour(endTimeHour);
                timePicker.setCurrentMinute(endTimeMinute);
            }
        }
        else {
            int startTimeHour = getIntent().getIntExtra("startTimeHour", 0) + 1;
            if (startTimeHour > 23) startTimeHour -= 24;
            int startTimeMinute = getIntent().getIntExtra("startTimeMinute", 0);
            timePicker.setCurrentHour(startTimeHour);
            timePicker.setCurrentMinute(startTimeMinute);
        }
        initializeUi();
    }
    
    private void initializeUi() {
        Button buttonBackEndttime = (Button) findViewById(R.id.nodeEditButtonBackEndtime);
        buttonBackEndttime.setOnClickListener(this);
        Button buttonCancelEndtime = (Button) findViewById(R.id.nodeEditButtonCancelEndtime);
        buttonCancelEndtime.setOnClickListener(this);
        Button buttonFinishEndtime = (Button) findViewById(R.id.nodeEditButtonFinishEndtime);
        buttonFinishEndtime.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.nodeEditButtonCancelEndtime:
            finish();
            break;
        case R.id.nodeEditButtonBackEndtime:
            Intent intentStart = new Intent();
            intentStart.putExtra("direction", NodeEditActivity.DIRECTION_TO_START);
            putEndtimeIntoIntent(intentStart);
            copyIntentContents(intentStart);
            setResult(RESULT_OK, intentStart);
            finish();
            break;
        case R.id.nodeEditButtonFinishEndtime:
            Intent intentParent = new Intent();
            intentParent.putExtra("direction", NodeEditActivity.DIRECTION_TO_PARENT);
            putEndtimeIntoIntent(intentParent);
            copyIntentContents(intentParent);
            setResult(RESULT_OK, intentParent);
            finish();
            break;

        default:
            break;
        }
    }
    
    private void putEndtimeIntoIntent(Intent intent) {
        if (openEnd.isChecked()) {
            intent.putExtra("endTimeHour", -1);
            intent.putExtra("endTimeMinute", -1);
        }
        else {
            intent.putExtra("endTimeHour", timePicker.getCurrentHour());
            intent.putExtra("endTimeMinute", timePicker.getCurrentMinute());
        }
    }

    private void copyIntentContents(Intent intent) {
        for (int day=OpeningHours.MONDAY; day <= OpeningHours.SUNDAY; day++) {
            if (getIntent().hasExtra(OpeningHours.weekDayToString(day))) {
                intent.putExtra(OpeningHours.weekDayToString(day), getIntent().getBooleanExtra(OpeningHours.weekDayToString(day), false));
            }
        }
        if (getIntent().hasExtra("startTimeHour")) {
            intent.putExtra("startTimeHour", getIntent().getIntExtra("startTimeHour", -2));
            intent.putExtra("startTimeMinute", getIntent().getIntExtra("startTimeMinute", -2));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) timePicker.setEnabled(false);
        else timePicker.setEnabled(true);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int[] time = { -1, -1 };
        if (!openEnd.isChecked()) {
            time[0] = timePicker.getCurrentHour();
            time[1] = timePicker.getCurrentMinute();
        }
        outState.putIntArray("endtime", time);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("endtime")) {
            int[] time = savedInstanceState.getIntArray("endtime");
            
            if (time[0] == -1) {
                timePicker.setCurrentHour(0);
                timePicker.setCurrentMinute(0);
                timePicker.setEnabled(false);
                openEnd.setChecked(true);
            }
            else {
                timePicker.setCurrentHour(time[0]);
                timePicker.setCurrentMinute(time[1]);
            }
        }
    }

}
