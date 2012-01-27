package org.yaoha;

import java.util.Calendar;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class NodeEditActivity extends Activity implements OnClickListener, OnTimeChangedListener, OnCheckedChangeListener {
    boolean rangeComplete = false;
    boolean insideOnTimeChangedCallback = false;
    static final int DIALOG_HOUR_RANGE = 0;
    private OpeningHours openingHours = new OpeningHours();
    private boolean[] weekDaysChecked = new boolean[7];
    View rootView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_edit);
        
        Button addDefinition = (Button) findViewById(R.id.buttonAddDefiniton);
        addDefinition.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.buttonAddDefiniton:
            saveCheckboxes(v);
            showDialog(DIALOG_HOUR_RANGE);
            break;
        case R.id.buttonDialogOk:
            addHourRange(v);
            break;

        default:
            break;
        }
    }
    
    private void addHourRange(View v) {
        
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
        case DIALOG_HOUR_RANGE:
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.node_edit_hour_range_dialog);
            dialog.setTitle("Hour range");
            
            TimePicker startTime = (TimePicker) dialog.findViewById(R.id.timePickerStartTime);
            TimePicker endTime = (TimePicker) dialog.findViewById(R.id.timePickerEndTime);
            startTime.setIs24HourView(true);
            endTime.setIs24HourView(true);
            startTime.setCurrentHour(8);
            startTime.setCurrentMinute(0);
            endTime.setCurrentHour(8);
            endTime.setCurrentMinute(0);
            startTime.setOnTimeChangedListener(this);
            endTime.setOnTimeChangedListener(this);
            
            CheckBox openEnd = (CheckBox) dialog.findViewById(R.id.checkBoxOpenEnd);
            openEnd.setOnCheckedChangeListener(this);
            
            break;

        default:
            dialog = null;
            break;
        }
        return dialog;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        if (insideOnTimeChangedCallback) return;
        insideOnTimeChangedCallback = true;
        TimePicker startTimePicker = (TimePicker) view.getRootView().findViewById(R.id.timePickerStartTime);
        TimePicker endTimePicker = (TimePicker) view.getRootView().findViewById(R.id.timePickerEndTime);
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        
        startTime.set(Calendar.HOUR_OF_DAY, startTimePicker.getCurrentHour());
        startTime.set(Calendar.MINUTE, startTimePicker.getCurrentMinute());
        endTime.set(Calendar.HOUR_OF_DAY, endTimePicker.getCurrentHour());
        endTime.set(Calendar.MINUTE, endTimePicker.getCurrentMinute());
        
        if (startTime.after(endTime)) {
            switch (view.getId()) {
            case R.id.timePickerStartTime:
                endTimePicker.setCurrentHour(startTimePicker.getCurrentHour());
                endTimePicker.setCurrentMinute(startTimePicker.getCurrentMinute());
                break;
            case R.id.timePickerEndTime:
                startTimePicker.setCurrentHour(endTimePicker.getCurrentHour());
                startTimePicker.setCurrentMinute(endTimePicker.getCurrentMinute());
                break;

            default:
                break;
            }
        }
        insideOnTimeChangedCallback = false;
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        if (arg0.getId() == R.id.checkBoxOpenEnd) {
            TimePicker endTimePicker = (TimePicker) arg0.getRootView().findViewById(R.id.timePickerEndTime);
            if (arg0.isChecked()) {
                TimePicker startTimePicker = (TimePicker) arg0.getRootView().findViewById(R.id.timePickerStartTime);
                endTimePicker.setEnabled(false);
                endTimePicker.setCurrentHour(startTimePicker.getCurrentHour());
                endTimePicker.setCurrentMinute(startTimePicker.getCurrentMinute());
            }
            else {
                endTimePicker.setEnabled(true);
            }
        }
    }
    
    private void saveCheckboxes(View v) {
        CheckBox weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxMonday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked.put(Calendar.MONDAY, true);
        else weekDaysChecked.put(Calendar.MONDAY, false);
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxTuesday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked.put(Calendar.TUESDAY, true);
        else weekDaysChecked.put(Calendar.TUESDAY, false);
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxWednesday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked.put(Calendar.WEDNESDAY, true);
        else weekDaysChecked.put(Calendar.WEDNESDAY, false);
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxThursday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked.put(Calendar.THURSDAY, true);
        else weekDaysChecked.put(Calendar.THURSDAY, false);
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxFriday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked.put(Calendar.FRIDAY, true);
        else weekDaysChecked.put(Calendar.FRIDAY, false);
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxSaturday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked.put(Calendar.SATURDAY, true);
        else weekDaysChecked.put(Calendar.SATURDAY, false);
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxSunday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked.put(Calendar.SUNDAY, true);
        else weekDaysChecked.put(Calendar.SUNDAY, false);
    }
}
