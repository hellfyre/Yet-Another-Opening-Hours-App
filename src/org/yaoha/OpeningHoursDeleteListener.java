package org.yaoha;

import java.util.Set;

import android.view.View;
import android.view.View.OnClickListener;

public class OpeningHoursDeleteListener implements OnClickListener {
    NodeEditActivity neact;
    HourRange range;
    Set<HourRange> day;
    
    public OpeningHoursDeleteListener(NodeEditActivity neact, HourRange range, Set<HourRange> day) {
        this.neact = neact;
        this.range = range;
        this.day = day;
    }

    @Override
    public void onClick(View v) {
        day.remove(range);
        neact.populateUiElementesWithOpeningHours();
    }

}
