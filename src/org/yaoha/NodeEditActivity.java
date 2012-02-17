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
import java.util.ArrayList;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class NodeEditActivity extends Activity implements OnClickListener, NodeReceiverInterface<OsmNode>, OsmNodeRetrieverListener, OsmNodeUploadListener, OnItemClickListener {
    private OsmNode osmNode;
    private ArrayList<GridView> gridViews;
    private static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private static final int DIALOG_UPLOAD_PROGRESS = 1;
    private static final int REQUEST_NODE_EDIT = 0;
    
    public static final int DIRECTION_TO_WEEK = 0;
    public static final int DIRECTION_TO_START = 1;
    public static final int DIRECTION_TO_END = 2;
    public static final int DIRECTION_TO_PARENT = 3;
    
    public NodeEditActivity() {
        osmNode = null;
        gridViews = new ArrayList<GridView>();
    }

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
        else {
            populateUiElementes();
        }
    }
    
    private void initializeUi() {
        Button addHours = (Button) findViewById(R.id.nodeEditButtonAddHourRanges);
        addHours.setOnClickListener(this);
        Button uploadChanges = (Button) findViewById(R.id.nodeEditButtonUploadChanges);
        uploadChanges.setOnClickListener(this);
        
        gridViews.add((GridView) findViewById(R.id.nodeEditGridViewMonday));
        gridViews.add((GridView) findViewById(R.id.nodeEditGridViewTuesday));
        gridViews.add((GridView) findViewById(R.id.nodeEditGridViewWednesday));
        gridViews.add((GridView) findViewById(R.id.nodeEditGridViewThursday));
        gridViews.add((GridView) findViewById(R.id.nodeEditGridViewFriday));
        gridViews.add((GridView) findViewById(R.id.nodeEditGridViewSaturday));
        gridViews.add((GridView) findViewById(R.id.nodeEditGridViewSunday));
        for (GridView gridView : gridViews) {
            gridView.setOnItemClickListener(this);
        }
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
                    updateAdapter(day);
                }
            }
            populateUiElementes();
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
    
    private void populateUiElementes() {
        if (osmNode == null) return;
        TextView openingHoursTextView = (TextView) findViewById(R.id.nodeEditTextViewOpeningHoursString);
        if (osmNode.openingHours.unparsable()) {
            openingHoursTextView.setText("Failed to parse: " + osmNode.getOpeningHoursString());
        }
        else {
            openingHoursTextView.setText(osmNode.openingHours.compileOpeningHoursString());
        }
    }
    
    private void populateGridViews() {
        for (int day = OpeningHours.MONDAY; day <= OpeningHours.SUNDAY; day++) {
            gridViews.get(day).setAdapter(new TextAdapter(this, osmNode.openingHours.getWeekDay(day)));
        }
    }
    
    private void updateAdapter(int day) {
        TextAdapter currentAdapter = (TextAdapter) gridViews.get(day).getAdapter();
        currentAdapter.update(osmNode.openingHours.getWeekDay(day));
        currentAdapter.notifyDataSetChanged();
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
                populateGridViews();
                populateUiElementes();
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

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        GridView currentGridView = (GridView) arg0;
        int day = gridViews.indexOf(arg0);
        HourRange selectedHourRange = (HourRange) currentGridView.getAdapter().getItem(arg2);
        osmNode.openingHours.removeHourRangeFromDay(selectedHourRange, day);
        updateAdapter(day);
        populateUiElementes();
    }
}
