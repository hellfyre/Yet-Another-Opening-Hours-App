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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class OpeningHours implements Iterable<TreeSet<HourRange>> {
    private ArrayList<TreeSet<HourRange>> weekDays = new ArrayList<TreeSet<HourRange>>();
    public static final int MONDAY = 0;
    public static final int TUESDAY = 1;
    public static final int WEDNESDAY = 2;
    public static final int THURSDAY = 3;
    public static final int FRIDAY = 4;
    public static final int SATURDAY = 5;
    public static final int SUNDAY = 6;
    private static Pattern openingHoursPattern = Pattern.compile("[0-9]{1,2}:[0-9]{2}[-|+][0-9]{0,2}[:]{0,1}[0-9]{0,2}");
    private int parseError = 0;
    private boolean unparsable = false;
    
    public OpeningHours() {
        for (int i = 0; i < 7; i++) {
            weekDays.add(new TreeSet<HourRange>());
        }
    }

    @Override
    public Iterator<TreeSet<HourRange>> iterator() {
        return weekDays.iterator();
    }
    
    private void clearDay(int weekDay) {
        weekDays.get(weekDay).clear();
    }
    
    public void addHourRangeToDay(HourRange hourRange, int weekDay) {
        TreeSet<HourRange> currentDay = weekDays.get(weekDay);
        if (!currentDay.isEmpty()) {
            for (HourRange hrToCompare : currentDay) {
                if (hrToCompare.overlaps(hourRange)) return;
            }
        }
        currentDay.add(hourRange);
    }
    
    public TreeSet<HourRange> getWeekDay(int weekDay) {
        TreeSet<HourRange> weekDayTreeSet = new TreeSet<HourRange>();
        for (HourRange hourRange : weekDays.get(weekDay)) {
            HourRange clone = (HourRange) hourRange.clone();
            weekDayTreeSet.add(clone);
        }
        return weekDayTreeSet;
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
            if (currentRowIndex > 0 && !(equalDayRanges.get(currentRowIndex).isEmpty())) {
                if (openingHoursString.length() > 0) {
                    openingHoursString += "; ";
                }
            }
            ArrayList<Integer> currentRow = equalDayMatrix.get(currentRowIndex);
            String currentWeekDayRangeString = "";
            currentWeekDayRangeString += weekDayToString(currentRow.get(0));
            int lastDay = currentRow.get(0);
            for (int currentDayIndex = 1; currentDayIndex < currentRow.size(); currentDayIndex++) {
                int currentDay = currentRow.get(currentDayIndex);
                if (currentDay == (lastDay + 1)) {
                    int nextDay = (currentDayIndex+1) < currentRow.size() ? currentRow.get(currentDayIndex + 1) : -1;
                    if (nextDay != (currentDay + 1)) {
                        currentWeekDayRangeString += "-" + weekDayToString(currentDay);
                    }
                }
                else {
                    currentWeekDayRangeString += "," + weekDayToString(currentDay);
                }
                lastDay = currentDay;
            }
            
            TreeSet<HourRange> currentHourRangeRow = equalDayRanges.get(currentRowIndex);
            if (!currentHourRangeRow.isEmpty()) {
                openingHoursString += currentWeekDayRangeString + " ";
                for (HourRange currentHourRange : currentHourRangeRow) {
                    openingHoursString += currentHourRange;
                    if (currentHourRange != currentHourRangeRow.last()) {
                        openingHoursString += ",";
                    }
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

    public boolean isEmpty() {
        boolean isEmpty = true;
        if (!weekDays.get(MONDAY).isEmpty()) isEmpty = false;
        if (!weekDays.get(TUESDAY).isEmpty()) isEmpty = false;
        if (!weekDays.get(WEDNESDAY).isEmpty()) isEmpty = false;
        if (!weekDays.get(THURSDAY).isEmpty()) isEmpty = false;
        if (!weekDays.get(FRIDAY).isEmpty()) isEmpty = false;
        if (!weekDays.get(SATURDAY).isEmpty()) isEmpty = false;
        if (!weekDays.get(SUNDAY).isEmpty()) isEmpty = false;
        return isEmpty;
    }
    
    void clearWeek() {
        for (int i = MONDAY; i <= SUNDAY; i++)
            clearDay(i);
    }
    
    public void parse(String openingHoursString) {
        clearWeek();
        unparsable = false;
        if (openingHoursString == null) return;
        parseError = 0;
        openingHoursString = openingHoursString.toLowerCase();

        if (openingHoursString.equals("24/7")) {
            HourRange hourRange = new HourRange(0, 0, 23, 59);
            for (int i = MONDAY; i <= SUNDAY; i++)
                addHourRangeToDay(hourRange, i);
        }
        else {
            String[] components = openingHoursString.split("[;][ ]{0,1}");
            try {
                for (String part : components) {
                    parseComponent(part);
                    parseError += part.length();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Log.d(NodeEditActivity.class.getSimpleName(), e.getMessage());
//                clearWeek();
                unparsable = true;
            }
        }
    }
    
    boolean unparsable() {
        return unparsable;
    }
    
    private void parseComponent(String part) throws java.text.ParseException {
        if (part.equals("")) {
            return;
        }
        if (part.substring(0, 1).matches("[mtwfs]")) {
            parseWeekDayRange(part);
        }
        else if (part.substring(0, 1).matches("[0-9]")) {
            parseHourDayRange(part);
        }
        else {
            throw new java.text.ParseException("Part " + part + " not parsable: Doesn't start with a weekday nor with a time.", parseError);
        }
    }

    private void parseHourDayRange(String part) throws java.text.ParseException {
        ArrayList<HourRange> hours = parseHours(part);
        for (HourRange hourRange : hours) {
            addHourRangeToDay(hourRange, MONDAY);
            addHourRangeToDay(hourRange, TUESDAY);
            addHourRangeToDay(hourRange, WEDNESDAY);
            addHourRangeToDay(hourRange, THURSDAY);
            addHourRangeToDay(hourRange, FRIDAY);
            addHourRangeToDay(hourRange, SATURDAY);
            addHourRangeToDay(hourRange, SUNDAY);
        }
    }

    private void parseWeekDayRange(String part) throws java.text.ParseException {
        String[] weekDayComponents = part.split(" ");
        if (weekDayComponents.length != 2) {
            throw new java.text.ParseException("Component " + part + " not parsable: Should contain 2 parts (week day range, e.g. Mo-Fr and hour ranges, e.g. 08:00-18:00) divided by a space.", parseError);
        }
        ArrayList<HourRange> hours = parseHours(weekDayComponents[1]);
        if (hours == null) return;
        ArrayList<Integer> weekDays = parseDays(weekDayComponents[0]);
        
        for (Integer weekDayIndex : weekDays) {
            for (HourRange hourRange : hours) {
                addHourRangeToDay(hourRange, weekDayIndex);
            }
        }
    }

    private ArrayList<HourRange> parseHours(String rawHourRange) throws java.text.ParseException {
        if (rawHourRange.equals("off")) {
            return null;
        }
        ArrayList<HourRange> hours = new ArrayList<HourRange>();
        String[] hourRanges = rawHourRange.split(",");
        for (String hourRange : hourRanges) {
            Matcher regularOpeningHoursMatcher = openingHoursPattern.matcher(hourRange);
            if (!regularOpeningHoursMatcher.matches()) throw new java.text.ParseException("Hour range " + hourRange + " not parsable: Doesn't match regular expression.", parseError);
            hours.add(new HourRange(hourRange));
        }
        return hours;
    }

    private ArrayList<Integer> parseDays(String rawDayRange) throws java.text.ParseException {
        ArrayList<Integer> weekDays = new ArrayList<Integer>();
        String[] commaSeparatedDays = rawDayRange.split(",");
        for (String commaDay : commaSeparatedDays) {
            if (commaDay.contains("-")) {
                String[] dashSeparatedDays = commaDay.split("-");
                if (dashSeparatedDays.length != 2) throw new java.text.ParseException("Day range " + commaDay + " not parsable: Should contain exactly two weekdays separated by a dash (e.g. Mo-Fr).", parseError);
                Integer firstIntWeekDay = stringToWeekDay(dashSeparatedDays[0]);
                if (firstIntWeekDay == -1) throw new java.text.ParseException("Week day " + dashSeparatedDays[0] + " not parsable: Doesn't exist.", parseError);
                Integer secondIntWeekDay = stringToWeekDay(dashSeparatedDays[1]);
                if (secondIntWeekDay == -1) throw new java.text.ParseException("Week day " + dashSeparatedDays[1] + " not parsable: Doesn't exist.", parseError);
                int i = firstIntWeekDay;
                while (i != secondIntWeekDay) {
                    weekDays.add(i);
                    i++;
                    if (i>6) i=0;
                }
                weekDays.add(secondIntWeekDay);
            }
            else {
                Integer weekDay = stringToWeekDay(commaDay);
                if (weekDay == -1) throw new java.text.ParseException("Week day " + commaDay + " not parsable: Doesn't exist.", parseError);
                weekDays.add(weekDay);
            }
        }
        return weekDays;
    }

}
