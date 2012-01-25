package org.yaoha;

import java.util.ArrayList;
import java.util.Iterator;

public class OpeningHours implements Iterable<ArrayList<HourRange>> {
    private ArrayList<ArrayList<HourRange>> weekDays = new ArrayList<ArrayList<HourRange>>();
    public static final int MONDAY = 0;
    public static final int TUESDAY = 1;
    public static final int WEDNESDAY = 2;
    public static final int THURSDAY = 3;
    public static final int FRIDAY = 4;
    public static final int SATURDAY = 5;
    public static final int SUNDAY = 6;
    
    public OpeningHours() {
        for (int i = 0; i < 7; i++) {
            weekDays.add(null);
        }
    }

    @Override
    public Iterator<ArrayList<HourRange>> iterator() {
        return weekDays.iterator();
    }
    
    public void set(int weekDay, ArrayList<HourRange> hourRanges) {
        weekDays.set(weekDay, hourRanges);
    }
    
    public void set(String weekDay, ArrayList<HourRange> hourRanges) {
        weekDay = weekDay.toLowerCase();
        if (weekDay.equals("mo") || weekDay.equals("monday")) {
            weekDays.set(MONDAY, hourRanges);
        }
        else if (weekDay.equals("tu") || weekDay.equals("tuesday")) {
            weekDays.set(TUESDAY, hourRanges);
        }
        else if (weekDay.equals("we") || weekDay.equals("wednesday")) {
            weekDays.set(WEDNESDAY, hourRanges);
        }
        else if (weekDay.equals("th") || weekDay.equals("thursday")) {
            weekDays.set(THURSDAY, hourRanges);
        }
        else if (weekDay.equals("fr") || weekDay.equals("friday")) {
            weekDays.set(FRIDAY, hourRanges);
        }
        else if (weekDay.equals("sa") || weekDay.equals("saturday")) {
            weekDays.set(SATURDAY, hourRanges);
        }
        else if (weekDay.equals("su") || weekDay.equals("sunday")) {
            weekDays.set(SUNDAY, hourRanges);
        }
        else {
            return;
        }
    }
    
    public ArrayList<HourRange> get(int weekDay) {
        return weekDays.get(weekDay);
    }
    
    public ArrayList<HourRange> get(String weekDay) {
        weekDay = weekDay.toLowerCase();
        if (weekDay.equals("mo") || weekDay.equals("monday")) {
            return weekDays.get(MONDAY);
        }
        else if (weekDay.equals("tu") || weekDay.equals("tuesday")) {
            return weekDays.get(TUESDAY);
        }
        else if (weekDay.equals("we") || weekDay.equals("wednesday")) {
            return weekDays.get(WEDNESDAY);
        }
        else if (weekDay.equals("th") || weekDay.equals("thursday")) {
            return weekDays.get(THURSDAY);
        }
        else if (weekDay.equals("fr") || weekDay.equals("friday")) {
            return weekDays.get(FRIDAY);
        }
        else if (weekDay.equals("sa") || weekDay.equals("saturday")) {
            return weekDays.get(SATURDAY);
        }
        else if (weekDay.equals("su") || weekDay.equals("sunday")) {
            return weekDays.get(SUNDAY);
        }
        else {
            return null;
        }
    }
    
    public String compileOpeningHoursString() {
        // TODO: create this
        return null;
    }

}
