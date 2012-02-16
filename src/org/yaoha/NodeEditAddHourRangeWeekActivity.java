package org.yaoha;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class NodeEditAddHourRangeWeekActivity extends Activity implements OnClickListener {
    ArrayList<CheckBox> checkBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_edit_add_hour_range_weekdays);
        
        checkBoxes = new ArrayList<CheckBox>();
        checkBoxes.add(OpeningHours.MONDAY, ((CheckBox) findViewById(R.id.nodeEditCheckBoxMonday)));
        checkBoxes.add(OpeningHours.TUESDAY, ((CheckBox) findViewById(R.id.nodeEditCheckBoxTuesday)));
        checkBoxes.add(OpeningHours.WEDNESDAY, ((CheckBox) findViewById(R.id.nodeEditCheckBoxWednesday)));
        checkBoxes.add(OpeningHours.THURSDAY, ((CheckBox) findViewById(R.id.nodeEditCheckBoxThursday)));
        checkBoxes.add(OpeningHours.FRIDAY, ((CheckBox) findViewById(R.id.nodeEditCheckBoxFriday)));
        checkBoxes.add(OpeningHours.SATURDAY, ((CheckBox) findViewById(R.id.nodeEditCheckBoxSaturday)));
        checkBoxes.add(OpeningHours.SUNDAY, ((CheckBox) findViewById(R.id.nodeEditCheckBoxSunday)));
        for (int day = OpeningHours.MONDAY; day <= OpeningHours.SUNDAY; day++) {
            if (getIntent().getBooleanExtra(OpeningHours.weekDayToString(day), false)) {
                checkBoxes.get(day).setChecked(true);
                getIntent().putExtra(OpeningHours.weekDayToString(day), false);
            }
        }
        initializeUi();
    }

    private void initializeUi() {
        Button buttonCancelWeekdays = (Button) findViewById(R.id.nodeEditButtonCancelWeek);
        buttonCancelWeekdays.setOnClickListener(this);
        Button buttonNextWeekdays = (Button) findViewById(R.id.nodeEditButtonNextWeek);
        buttonNextWeekdays.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.nodeEditButtonCancelWeek:
            finish();
            break;
        case R.id.nodeEditButtonNextWeek:
            Intent intentStart = new Intent();
            intentStart.putExtra("direction", NodeEditActivity.DIRECTION_TO_START);
            putCheckboxesIntoIntent(intentStart);
            copyIntentContents(intentStart);
            setResult(RESULT_OK, intentStart);
            finish();
            break;
        default:
            break;
        }
    }
    
    private void putCheckboxesIntoIntent(Intent intent) {
        for (int day = OpeningHours.MONDAY; day <= OpeningHours.SUNDAY; day++) {
            if (checkBoxes.get(day).isChecked()) intent.putExtra(OpeningHours.weekDayToString(day), true);
        }
    }
    
    private void copyIntentContents(Intent intent) {
        if (getIntent().hasExtra("startTimeHour") && getIntent().hasExtra("startTimeMinute")) {
            intent.putExtra("startTimeHour", getIntent().getIntExtra("startTimeHour", -2));
            intent.putExtra("startTimeMinute", getIntent().getIntExtra("startTimeMinute", -2));
        }
        if (getIntent().hasExtra("endTimeHour") && getIntent().hasExtra("endTimeMinute")) {
            intent.putExtra("endTimeHour", getIntent().getIntExtra("endTimeHour", -2));
            intent.putExtra("endTimeMinute", getIntent().getIntExtra("endTimeMinute", -2));
        }
    }

}
