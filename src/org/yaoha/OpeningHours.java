package org.yaoha;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class OpeningHours implements Iterable<TreeSet<HourRange>> {
    private ArrayList<TreeSet<HourRange>> weekDays = new ArrayList<TreeSet<HourRange>>();
    public static final int MONDAY = 0;
    public static final int TUESDAY = 1;
    public static final int WEDNESDAY = 2;
    public static final int THURSDAY = 3;
    public static final int FRIDAY = 4;
    public static final int SATURDAY = 5;
    public static final int SUNDAY = 6;
    
    public OpeningHours() {
        for (int i = 0; i < 7; i++) {
            weekDays.add(new TreeSet<HourRange>());
        }
    }

    @Override
    public Iterator<TreeSet<HourRange>> iterator() {
        return weekDays.iterator();
    }
    
    public void set(int weekDay, TreeSet<HourRange> hourRanges) {
        weekDays.set(weekDay, hourRanges);
    }
    
    public void set(String weekDay, TreeSet<HourRange> hourRanges) {
        weekDays.set(stringToWeekDay(weekDay), hourRanges);
    }
    
    public TreeSet<HourRange> get(int weekDay) {
        return weekDays.get(weekDay);
    }
    
    public TreeSet<HourRange> get(String weekDay) {
        return weekDays.get(stringToWeekDay(weekDay));
    }
    
    public String compileOpeningHoursString() {
        ArrayList<Integer> toCheck = new ArrayList<Integer>();
        for (int day = MONDAY; day <= SUNDAY; day++) {
            toCheck.add(day);
        }
        ArrayList<ArrayList<Integer>> equalDayMatrix = new ArrayList<ArrayList<Integer>>();
        ArrayList<TreeSet<HourRange>> equalDayRanges = new ArrayList<TreeSet<HourRange>>();
        while (!toCheck.isEmpty()) {
            int currentDay = toCheck.get(0);
            ArrayList<Integer> equalDayRow = new ArrayList<Integer>();
            equalDayRow.add(currentDay);
            equalDayRanges.add(weekDays.get(currentDay)); // Caution: we add a pointer to the actual HourRanges TreeSet in the weekDays ArrayList! Don't alter any data in equalDayRows!
            toCheck.remove(toCheck.indexOf(currentDay));
            int otherDayIndex = 0;
            while (otherDayIndex < toCheck.size()) {
                int otherDay = toCheck.get(otherDayIndex);
                TreeSet<HourRange> currentDaySet = weekDays.get(currentDay);
                TreeSet<HourRange> otherDaySet = weekDays.get(otherDay);
                if (currentDaySet.equals(otherDaySet)) {
                    equalDayRow.add(otherDay);
                    toCheck.remove(toCheck.indexOf(otherDay));
                }
                else {
                    otherDayIndex++;
                }
            }
            equalDayMatrix.add(equalDayRow);
        }
        
        // sorted
        String openingHoursString = "";
        for (int currentRowIndex = 0; currentRowIndex < equalDayMatrix.size(); currentRowIndex++) {
            if (currentRowIndex > 0) {
                openingHoursString += "; ";
            }
            ArrayList<Integer> currentRow = equalDayMatrix.get(currentRowIndex);
            openingHoursString += weekDayToString(currentRow.get(0));
            int lastDay = currentRow.get(0);
            for (int currentDayIndex = 1; currentDayIndex < currentRow.size(); currentDayIndex++) {
                int currentDay = currentRow.get(currentDayIndex);
                if (currentDay == (lastDay + 1)) {
                    int nextDay = (currentDayIndex+1) < currentRow.size() ? currentRow.get(currentDayIndex + 1) : -1;
                    if (nextDay != (currentDay + 1)) {
                        openingHoursString += "-" + weekDayToString(currentDay);
                    }
                }
                else {
                    openingHoursString += "," + weekDayToString(currentDay);
                }
                lastDay = currentDay;
            }
            
            TreeSet<HourRange> currentHourRangeRow = equalDayRanges.get(currentRowIndex);
            if (currentHourRangeRow.isEmpty()) {
                openingHoursString += " off";
            }
            else {
                HourRange currentHourRange = currentHourRangeRow.first();
                openingHoursString += " " + currentHourRange;
                while ( (currentHourRange = currentHourRangeRow.higher(currentHourRange)) != null) {
                    openingHoursString += "," + currentHourRange;
                }
            }
        }
        return openingHoursString;
    }
    
    public String weekDayToString(int weekDay) {
        switch (weekDay) {
        case MONDAY:
            return "Mo";
        case TUESDAY:
            return "Tu";
        case WEDNESDAY:
            return "We";
        case THURSDAY:
            return "Th";
        case FRIDAY:
            return "Fr";
        case SATURDAY:
            return "Sa";
        case SUNDAY:
            return "Su";
        default:
            return null;
        }
    }
    
    public int stringToWeekDay(String weekDay) {
        weekDay = weekDay.toLowerCase();
        if (weekDay.equals("mo") || weekDay.equals("monday")) {
            return MONDAY;
        }
        else if (weekDay.equals("tu") || weekDay.equals("tuesday")) {
            return TUESDAY;
        }
        else if (weekDay.equals("we") || weekDay.equals("wednesday")) {
            return WEDNESDAY;
        }
        else if (weekDay.equals("th") || weekDay.equals("thursday")) {
            return THURSDAY;
        }
        else if (weekDay.equals("fr") || weekDay.equals("friday")) {
            return FRIDAY;
        }
        else if (weekDay.equals("sa") || weekDay.equals("saturday")) {
            return SATURDAY;
        }
        else if (weekDay.equals("su") || weekDay.equals("sunday")) {
            return SUNDAY;
        }
        else {
            return -1;
        }
    }

}
