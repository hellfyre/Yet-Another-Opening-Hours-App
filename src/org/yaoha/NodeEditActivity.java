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

public class NodeEditActivity extends Activity implements OnClickListener, OnTimeChangedListener, OnCheckedChangeListener, NodeReceiverInterface<OsmNode>, OsmNodeRetrieverListener {
    OsmNode osmNode = null;
    boolean rangeComplete = false;
    boolean insideOnTimeChangedCallback = false;
    static final int DIALOG_HOUR_RANGE = 0;
    static final int DIALOG_PROGRESS = 1;
    private boolean[] weekDaysChecked = new boolean[7];
    View rootView;
    static final int[] boxesMoFr = {
            R.id.checkBoxMonday,
            R.id.checkBoxTuesday,
            R.id.checkBoxWednesday,
            R.id.checkBoxThursday,
            R.id.checkBoxFriday
    };
    static final int[] boxesSaSu = {
            R.id.checkBoxSaturday,
            R.id.checkBoxSunday
    };
    static final int[] allBoxes = {
            R.id.checkBoxMonday,
            R.id.checkBoxTuesday,
            R.id.checkBoxWednesday,
            R.id.checkBoxThursday,
            R.id.checkBoxFriday,
            R.id.checkBoxSaturday,
            R.id.checkBoxSunday
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_edit);
        
        Button addDefinition = (Button) findViewById(R.id.buttonAddDefiniton);
        addDefinition.setOnClickListener(this);
        Button checkMoFr = (Button) findViewById(R.id.buttonSelectMoFr);
        checkMoFr.setOnClickListener(this);
        Button checkSaSu = (Button) findViewById(R.id.buttonSelectSaSu);
        checkSaSu.setOnClickListener(this);
        Button transmitChanges = (Button) findViewById(R.id.buttonTransmitChanges);
        transmitChanges.setOnClickListener(this);
        
        Intent intent = getIntent();
        int nodeId = intent.getExtras().getInt("id");
        URI requestUri = ApiConnector.getRequestUriApiGetNode(String.valueOf(nodeId));
        if (requestUri != null) {
            showDialog(DIALOG_PROGRESS);
            OsmNodeRetrieverTask retrieverTask = new OsmNodeRetrieverTask(requestUri, this);
            retrieverTask.addListener(this);
            retrieverTask.execute();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.buttonAddDefiniton:
            if (!anyBoxChecked(allBoxes)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("No days selected.");
                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return;
            }
            saveCheckboxes(v);
            rootView = v.getRootView();
            showDialog(DIALOG_HOUR_RANGE);
            break;
        case R.id.buttonDialogOk:
            addHourRange(v);
            TextView ohString = (TextView) rootView.findViewById(R.id.TextViewOpeningHoursString);
            ohString.setText(osmNode.getPointerToOpeningHours().compileOpeningHoursString());
            break;
        case R.id.buttonSelectMoFr:
            if (anyBoxChecked(boxesMoFr)) {
                uncheckCheckBoxes(boxesMoFr);
            }
            else {
                checkCheckBoxes(boxesMoFr);
            }
            break;
        case R.id.buttonSelectSaSu:
            if (anyBoxChecked(boxesSaSu)) {
                uncheckCheckBoxes(boxesSaSu);
            }
            else {
                checkCheckBoxes(boxesSaSu);
            }
            break;
        case R.id.buttonTransmitChanges:
            osmNode.setOpening_hours(osmNode.getPointerToOpeningHours().compileOpeningHoursString());
            try {
                String xml_ouput = osmNode.serialize();
                OsmNodeDbHelper.getInstance().put(osmNode, true);
                // TODO transmit xml_output to OSM
            }
            catch (Exception e) {
                Log.d(this.getClass().getSimpleName(), "A node could not be converted into xml and be sent to osm");
            }
            break;

        default:
            break;
        }
    }
    
    private void addHourRange(View v) {
        TimePicker startTimePicker = (TimePicker) v.getRootView().findViewById(R.id.timePickerStartTime);
        int endTimeHour = -1;
        int endTimeMinute = -1;
        CheckBox openEnd = (CheckBox) v.getRootView().findViewById(R.id.checkBoxOpenEnd);
        if (!openEnd.isChecked()) {
            TimePicker endTimePicker = (TimePicker) v.getRootView().findViewById(R.id.timePickerEndTime);
            endTimeHour = endTimePicker.getCurrentHour();
            endTimeMinute = endTimePicker.getCurrentMinute();
        }
        HourRange newHourRange = new HourRange(startTimePicker.getCurrentHour(), startTimePicker.getCurrentMinute(), endTimeHour, endTimeMinute);
        
        for (int weekDay = OpeningHours.MONDAY; weekDay <= OpeningHours.SUNDAY; weekDay++) {
            if (!weekDaysChecked[weekDay]) continue;
            TreeSet<HourRange> hourRanges = osmNode.getPointerToOpeningHours().getDay(weekDay);
            for (HourRange hourRange : hourRanges) {
                if (hourRange.overlaps(newHourRange)) {
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
            hourRanges.add(newHourRange);
        }
        populateUiElementesWithOpeningHours();
        removeDialog(DIALOG_HOUR_RANGE);
    }
    
    void populateUiElementesWithOpeningHours() {
        for (int weekDay = OpeningHours.MONDAY; weekDay <= OpeningHours.SUNDAY; weekDay++) {
            TreeSet<HourRange> hourRanges = osmNode.getPointerToOpeningHours().getDay(weekDay);
            int linearLayoutId = 0;
            switch (weekDay) {
            case OpeningHours.MONDAY:
                linearLayoutId = R.id.linearLayoutMonday;
                break;
            case OpeningHours.TUESDAY:
                linearLayoutId = R.id.linearLayoutTuesday;
                break;
            case OpeningHours.WEDNESDAY:
                linearLayoutId = R.id.linearLayoutWednesday;
                break;
            case OpeningHours.THURSDAY:
                linearLayoutId = R.id.linearLayoutThursday;
                break;
            case OpeningHours.FRIDAY:
                linearLayoutId = R.id.linearLayoutFriday;
                break;
            case OpeningHours.SATURDAY:
                linearLayoutId = R.id.linearLayoutSaturday;
                break;
            case OpeningHours.SUNDAY:
                linearLayoutId = R.id.linearLayoutSunday;
                break;
            default:
                break;
            }
            
            LinearLayout linlay = (LinearLayout) findViewById(linearLayoutId);
            while (linlay.getChildCount() > 1)
                linlay.removeViewAt(1);
            for (HourRange hourRange : hourRanges) {
                TextView opening_hour_in_layout = new TextView(getBaseContext());
                opening_hour_in_layout.setText(hourRange.toString());
                opening_hour_in_layout.setOnClickListener(new OpeningHoursDeleteListener(this, hourRange, hourRanges));
                
                linlay.addView(opening_hour_in_layout);
            }
            
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
        case DIALOG_PROGRESS:
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Retrieving node ...");
            progressDialog.setIndeterminate(true);
            return progressDialog;

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
    
    private void saveCheckboxes(View v) {
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
    }
    
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
        final TextView ohString = (TextView) findViewById(R.id.TextViewOpeningHoursString);
        ohString.post(new Runnable() {
            @Override
            public void run() {
                String text = osmNode.getOpening_hours();
                if (osmNode.getPointerToOpeningHours().hasParsingFailed())
                    text = text + " failed to parse";
                ohString.setText(text);
                populateUiElementesWithOpeningHours();
            }
        });
        removeDialog(DIALOG_PROGRESS);
    }
}
