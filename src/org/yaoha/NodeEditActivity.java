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

import java.net.URI;
import java.util.Calendar;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class NodeEditActivity extends Activity implements OnClickListener, OnTimeChangedListener, OnCheckedChangeListener, NodeReceiverInterface<OsmNode>, OsmNodeRetrieverListener, OsmNodeUploadListener {
    private OsmNode osmNode = null;
    private boolean rangeComplete = false;
    private boolean insideOnTimeChangedCallback = false;
    private static final int DIALOG_HOUR_RANGE = 0;
    private static final int DIALOG_DOWNLOAD_PROGRESS = 1;
    private static final int DIALOG_UPLOAD_PROGRESS = 2;
    private static final int REQUEST_NODE_EDIT = 0;
    
    public static final int DIRECTION_TO_WEEK = 0;
    public static final int DIRECTION_TO_START = 1;
    public static final int DIRECTION_TO_END = 2;
    public static final int DIRECTION_TO_PARENT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_edit);
        initializeUi();
        
        Intent intent = getIntent();
        int nodeId = intent.getExtras().getInt("id");
        URI requestUri = ApiConnector.getRequestUriApiGetNode(String.valueOf(nodeId));
        if (requestUri != null) {
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
            OsmNodeRetrieverTask retrieverTask = new OsmNodeRetrieverTask(requestUri, this);
            retrieverTask.addListener(this);
            retrieverTask.execute();
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.node_edit);
        initializeUi();
        if (osmNode != null) {
            populateUiElementesWithOpeningHours();
        }
    }
    
    private void initializeUi() {
        Button addDefinition = (Button) findViewById(R.id.buttonAddDefiniton);
        addDefinition.setOnClickListener(this);
        Button transmitChanges = (Button) findViewById(R.id.buttonTransmitChanges);
        transmitChanges.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.buttonAddDefiniton:
            Intent nodeEditIntent = new Intent(this, NodeEditAddHourRangeWeekActivity.class);
            startActivityForResult(nodeEditIntent, REQUEST_NODE_EDIT);
            break;
        case R.id.buttonTransmitChanges:
            if (!ApiConnector.isAuthenticated()) {
                // TODO: replace by dialog
                Toast.makeText(this, "Not authenticated. Aborting upload. Please authenticate first.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, OSMSettingsActivity.class));
                return;
            }
            showDialog(DIALOG_UPLOAD_PROGRESS);
            osmNode.commitOpeningHours();
            OsmNodeDbHelper.getInstance().put(osmNode, true);
            OsmNodeUploadTask uploadTask = new OsmNodeUploadTask();
            uploadTask.addReceiver(this);
            uploadTask.execute(osmNode);
            break;
        default:
            break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || requestCode != REQUEST_NODE_EDIT) return;
        switch (data.getIntExtra("direction", -1)) {
        case DIRECTION_TO_WEEK:
            Intent intentWeek = new Intent();
            copyIntentContents(data, intentWeek);
            intentWeek.setClass(this, NodeEditAddHourRangeWeekActivity.class);
            startActivityForResult(intentWeek, REQUEST_NODE_EDIT);
            break;
        case DIRECTION_TO_START:
            Intent intentStart = new Intent();
            copyIntentContents(data, intentStart);
            intentStart.setClass(this, NodeEditAddHourRangeStarttimeActivity.class);
            startActivityForResult(intentStart, REQUEST_NODE_EDIT);
            break;
        case DIRECTION_TO_END:
            Intent intentEnd = new Intent();
            copyIntentContents(data, intentEnd);
            intentEnd.setClass(this, NodeEditAddHourRangeEndtimeActivity.class);
            startActivityForResult(intentEnd, REQUEST_NODE_EDIT);
            break;
        case DIRECTION_TO_PARENT:
            int startingHour = data.getIntExtra("startTimeHour", 0);
            int startingMinute = data.getIntExtra("startTimeMinute", 0);
            int endingHour = data.getIntExtra("endTimeHour", 0);
            int endingMinute = data.getIntExtra("endTimeMinute", 0);
            HourRange newRange = new HourRange(startingHour, startingMinute, endingHour, endingMinute);
            // check for overlapping hour ranges before adding ANY new ranges
            for (int day = OpeningHours.MONDAY; day <= OpeningHours.SUNDAY; day++) {
                if (data.getBooleanExtra(OpeningHours.weekDayToString(day), false)) {
                    TreeSet<HourRange> hours = osmNode.openingHours.getWeekDay(day);
                    for (HourRange hourRange : hours) {
                        if (newRange.overlaps(hourRange)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage("This hour range overlaps with " + hourRange);
                            builder.setCancelable(false);
                            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog overlapAlert = builder.create();
                            overlapAlert.show();
                            return;
                        }
                    }
                }
            }
            for (int day = OpeningHours.MONDAY; day <= OpeningHours.SUNDAY; day++) {
                if (data.getBooleanExtra(OpeningHours.weekDayToString(day), false)) {
                    osmNode.openingHours.addHourRangeToDay(newRange, day);
                }
            }
            populateUiElementesWithOpeningHours();
            break;
        default:
            break;
        }
    }
    
    private void copyIntentContents(Intent fromIntent, Intent toIntent) {
        for (int day=OpeningHours.MONDAY; day <= OpeningHours.SUNDAY; day++) {
            if (fromIntent.hasExtra(OpeningHours.weekDayToString(day))) {
                toIntent.putExtra(OpeningHours.weekDayToString(day), fromIntent.getBooleanExtra(OpeningHours.weekDayToString(day), false));
            }
        }
        if (fromIntent.hasExtra("startTimeHour") && fromIntent.hasExtra("startTimeMinute")) {
            toIntent.putExtra("startTimeHour", fromIntent.getIntExtra("startTimeHour", -2));
            toIntent.putExtra("startTimeMinute", fromIntent.getIntExtra("startTimeMinute", -2));
        }
        if (fromIntent.hasExtra("endTimeHour") && fromIntent.hasExtra("endTimeMinute")) {
            toIntent.putExtra("endTimeHour", fromIntent.getIntExtra("endTimeHour", -2));
            toIntent.putExtra("endTimeMinute", fromIntent.getIntExtra("endTimeMinute", -2));
        }
    }
    
    void populateUiElementesWithOpeningHours() {
        if (osmNode == null) return;
        TextView openingHoursTextView = (TextView) findViewById(R.id.nodeEditTextViewOpeningHoursString);
        String openingHoursString = osmNode.getOpeningHoursString();
        if (osmNode.openingHours.unparsable()) openingHoursString = "Failed to parse: " + openingHoursString;
        openingHoursTextView.setText(openingHoursString);
        
        for (int weekDay = OpeningHours.MONDAY; weekDay <= OpeningHours.SUNDAY; weekDay++) {
            TreeSet<HourRange> hourRanges = osmNode.openingHours.getWeekDay(weekDay);
            int weekDayTextViewId = 0;
            switch (weekDay) {
            case OpeningHours.MONDAY:
                weekDayTextViewId = R.id.nodeEditTextViewHoursMonday;
                break;
            case OpeningHours.TUESDAY:
                weekDayTextViewId = R.id.nodeEditTextViewHoursTuesday;
                break;
            case OpeningHours.WEDNESDAY:
                weekDayTextViewId = R.id.nodeEditTextViewHoursWednesday;
                break;
            case OpeningHours.THURSDAY:
                weekDayTextViewId = R.id.nodeEditTextViewHoursThursday;
                break;
            case OpeningHours.FRIDAY:
                weekDayTextViewId = R.id.nodeEditTextViewHoursFriday;
                break;
            case OpeningHours.SATURDAY:
                weekDayTextViewId = R.id.nodeEditTextViewHoursSaturday;
                break;
            case OpeningHours.SUNDAY:
                weekDayTextViewId = R.id.nodeEditTextViewHoursSunday;
                break;
            default:
                break;
            }
            
            TextView weekDayTextView = (TextView) findViewById(weekDayTextViewId);
            String weekDayString = "";
            for (HourRange hourRange : hourRanges) {
                weekDayString += hourRange + " ";
            }
            weekDayTextView.setText(weekDayString);
            
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_HOUR_RANGE:
            Dialog dialog = new Dialog(this);
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
            Button dialogOk = (Button) dialog.findViewById(R.id.buttonDialogOk);
            dialogOk.setOnClickListener(this);
            return dialog;
        case DIALOG_DOWNLOAD_PROGRESS:
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Retrieving node ...");
            progressDialog.setIndeterminate(true);
            return progressDialog;
        case DIALOG_UPLOAD_PROGRESS:
            ProgressDialog uploadDialog = new ProgressDialog(this);
            uploadDialog.setMessage("Uploading node ...");
            uploadDialog.setIndeterminate(true);
            return uploadDialog;

        default:
            return null;
        }
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
    
    /*private void saveCheckboxes(View v) {
        CheckBox weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxMonday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked[OpeningHours.MONDAY] = true;
        else weekDaysChecked[OpeningHours.MONDAY] = false;
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxTuesday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked[OpeningHours.TUESDAY] = true;
        else weekDaysChecked[OpeningHours.TUESDAY] = false;
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxWednesday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked[OpeningHours.WEDNESDAY] = true;
        else weekDaysChecked[OpeningHours.WEDNESDAY] = false;
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxThursday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked[OpeningHours.THURSDAY] = true;
        else weekDaysChecked[OpeningHours.THURSDAY] = false;
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxFriday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked[OpeningHours.FRIDAY] = true;
        else weekDaysChecked[OpeningHours.FRIDAY] = false;
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxSaturday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked[OpeningHours.SATURDAY] = true;
        else weekDaysChecked[OpeningHours.SATURDAY] = false;
        weekDayCheckBox = (CheckBox) v.getRootView().findViewById(R.id.checkBoxSunday);
        if (weekDayCheckBox.isChecked()) weekDaysChecked[OpeningHours.SUNDAY] = true;
        else weekDaysChecked[OpeningHours.SUNDAY] = false;
    }*/
    
    private boolean anyBoxChecked(int ... checkBoxIds) {
        boolean anyBoxChecked = false;
        for (int checkBoxId : checkBoxIds) {
            CheckBox box = (CheckBox) findViewById(checkBoxId);
            if (box.isChecked()) anyBoxChecked = true;
        }
        return anyBoxChecked;
    }
    
    private void checkCheckBoxes(int ... checkBoxIds) {
        for (int checkBoxId : checkBoxIds) {
            CheckBox box = (CheckBox) findViewById(checkBoxId);
            box.setChecked(true);
        }
    }
    
    private void uncheckCheckBoxes(int ... checkBoxIds) {
        for (int checkBoxId : checkBoxIds) {
            CheckBox box = (CheckBox) findViewById(checkBoxId);
            box.setChecked(false);
        }
    }

    @Override
    public void put(OsmNode value) {
        this.osmNode = value;
    }

    @Override
    public void onAllRequestsProcessed() {
        final TextView ohString = (TextView) findViewById(R.id.nodeEditTextViewOpeningHoursString);
        ohString.post(new Runnable() {
            @Override
            public void run() {
                populateUiElementesWithOpeningHours();
            }
        });
        removeDialog(DIALOG_DOWNLOAD_PROGRESS);
    }

    @Override
    public void requeryBoundingBox() {
    }

    @Override
    public void onNodeUploaded(String result) {
        removeDialog(DIALOG_UPLOAD_PROGRESS);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }
}
