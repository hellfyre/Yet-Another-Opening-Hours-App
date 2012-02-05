package org.yaoha;

import java.util.Set;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

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
        TextView ohString = (TextView) neact.findViewById(R.id.TextViewOpeningHoursString);
        ohString.setText(neact.osmNode.getPointerToOpeningHours().compileOpeningHoursString());
    }

}
