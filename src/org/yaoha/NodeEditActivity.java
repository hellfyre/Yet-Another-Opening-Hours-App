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
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class NodeEditActivity extends Activity implements OnClickListener, NodeReceiverInterface<OsmNode>, OsmNodeRetrieverListener, OsmNodeUploadListener {
    private OsmNode osmNode = null;
    private static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private static final int DIALOG_UPLOAD_PROGRESS = 1;
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
        
        getNode(getIntent());
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.node_edit);
        initializeUi();
        if (osmNode == null) {
            getNode(getIntent());
        }
        populateUiElementesWithOpeningHours();
    }
    
    private void initializeUi() {
        Button addDefinition = (Button) findViewById(R.id.nodeEditButtonAddHourRanges);
        addDefinition.setOnClickListener(this);
        Button transmitChanges = (Button) findViewById(R.id.nodeEditButtonUploadChanges);
        transmitChanges.setOnClickListener(this);
    }
    
    private void getNode(Intent intent) {
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
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.nodeEditButtonAddHourRanges:
            Intent nodeEditIntent = new Intent(this, NodeEditAddHourRangeWeekActivity.class);
            startActivityForResult(nodeEditIntent, REQUEST_NODE_EDIT);
            break;
        case R.id.nodeEditButtonUploadChanges:
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
        
        GridView gridViewMonday = (GridView) findViewById(R.id.nodeEditGridViewMonday);
        gridViewMonday.setAdapter(new TextAdapter(this, osmNode.openingHours.getWeekDay(OpeningHours.MONDAY)));
        GridView gridViewTuesday = (GridView) findViewById(R.id.nodeEditGridViewTuesday);
        gridViewTuesday.setAdapter(new TextAdapter(this, osmNode.openingHours.getWeekDay(OpeningHours.TUESDAY)));
        GridView gridViewWednesday = (GridView) findViewById(R.id.nodeEditGridViewWednesday);
        gridViewWednesday.setAdapter(new TextAdapter(this, osmNode.openingHours.getWeekDay(OpeningHours.WEDNESDAY)));
        GridView gridViewThursday = (GridView) findViewById(R.id.nodeEditGridViewThursday);
        gridViewThursday.setAdapter(new TextAdapter(this, osmNode.openingHours.getWeekDay(OpeningHours.THURSDAY)));
        GridView gridViewFriday = (GridView) findViewById(R.id.nodeEditGridViewFriday);
        gridViewFriday.setAdapter(new TextAdapter(this, osmNode.openingHours.getWeekDay(OpeningHours.FRIDAY)));
        GridView gridViewSaturday = (GridView) findViewById(R.id.nodeEditGridViewSaturday);
        gridViewSaturday.setAdapter(new TextAdapter(this, osmNode.openingHours.getWeekDay(OpeningHours.SATURDAY)));
        GridView gridViewSunday = (GridView) findViewById(R.id.nodeEditGridViewSunday);
        gridViewSunday.setAdapter(new TextAdapter(this, osmNode.openingHours.getWeekDay(OpeningHours.SUNDAY)));
        
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
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
