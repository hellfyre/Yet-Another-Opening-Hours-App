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

import java.util.Calendar;

public class HourRange implements Comparable<HourRange> {
    private int startingHour;
    private int startingMinute;
    private int endingHour;
    private int endingMinute;
    
    public HourRange(String range) {
        parseRange(range);
    }
    
    public HourRange(int startingHour, int startingMinute, int endingHour, int endingMinute) {
        this.startingHour = startingHour;
        this.startingMinute = startingMinute;
        this.endingHour = endingHour;
        this.endingMinute = endingMinute;
    }
    
    private void parseRange(String range) {
        String start = "";
        if (!range.contains("+")) {
            String[] startEnd = range.split("-");
            start = startEnd[0];
            
            String[] hourMinute = startEnd[1].split(":");
            endingHour = Integer.parseInt(hourMinute[0]);
            endingMinute = Integer.parseInt(hourMinute[1]);
        }
        else {
            start = range.replace("+", "");
            endingHour = endingMinute = -1; 
        }
        String[] hourMinute = start.split(":");
        startingHour = Integer.parseInt(hourMinute[0]);
        startingMinute = Integer.parseInt(hourMinute[1]);
    }
    
    public boolean overlaps(HourRange range1) {
        Calendar startTime0 = Calendar.getInstance();
        Calendar endTime0 = Calendar.getInstance();
        Calendar startTime1 = Calendar.getInstance();
        Calendar endTime1 = Calendar.getInstance();
        startTime0.clear();
        endTime0.clear();
        startTime1.clear();
        endTime1.clear();
        startTime0.set(Calendar.HOUR_OF_DAY, startingHour);
        startTime0.set(Calendar.MINUTE, startingMinute);
        endTime0.set(Calendar.HOUR_OF_DAY, endingHour);
        endTime0.set(Calendar.MINUTE, endingMinute);
        startTime1.set(Calendar.HOUR_OF_DAY, range1.getStartingHour());
        startTime1.set(Calendar.MINUTE, range1.getStartingMinute());
        endTime1.set(Calendar.HOUR_OF_DAY, range1.getEndingHour());
        endTime1.set(Calendar.MINUTE, range1.getEndingMinute());
        
        if (startTime1.before(startTime0) && endTime1.before(startTime0)) {
            return false;
        }
        if (endTime1.after(endTime0) && startTime1.after(endTime0)) {
            return false;
        }
        
        return true;
    }
    
    public boolean completelyAfter(HourRange range1) {
        Calendar startTime0 = Calendar.getInstance();
        Calendar endTime1 = Calendar.getInstance();
        startTime0.clear();
        endTime1.clear();
        startTime0.set(Calendar.HOUR_OF_DAY, startingHour);
        startTime0.set(Calendar.MINUTE, startingMinute);
        endTime1.set(Calendar.HOUR_OF_DAY, range1.getEndingHour());
        endTime1.set(Calendar.MINUTE, range1.getEndingMinute());
        
        return startTime0.after(endTime1);
    }
    
    public boolean completelyBefore(HourRange range1) {
        Calendar startTime1 = Calendar.getInstance();
        Calendar endTime0 = Calendar.getInstance();
        startTime1.clear();
        endTime0.clear();
        startTime1.set(Calendar.HOUR_OF_DAY, range1.getStartingHour());
        startTime1.set(Calendar.MINUTE, range1.getStartingMinute());
        endTime0.set(Calendar.HOUR_OF_DAY, endingHour);
        endTime0.set(Calendar.MINUTE, endingMinute);
        
        return endTime0.before(startTime1);
    }
    
    public boolean after(HourRange range1) {
        Calendar startTime0 = Calendar.getInstance();
        Calendar startTime1 = Calendar.getInstance();
        startTime0.clear();
        startTime1.clear();
        startTime0.set(Calendar.HOUR_OF_DAY, startingHour);
        startTime0.set(Calendar.MINUTE, startingMinute);
        startTime1.set(Calendar.HOUR_OF_DAY, range1.getStartingHour());
        startTime1.set(Calendar.MINUTE, range1.getStartingMinute());
        
        return startTime0.after(startTime1);
    }
    
    public boolean before(HourRange range1) {
        Calendar startTime0 = Calendar.getInstance();
        Calendar startTime1 = Calendar.getInstance();
        startTime0.clear();
        startTime1.clear();
        startTime0.set(Calendar.HOUR_OF_DAY, startingHour);
        startTime0.set(Calendar.MINUTE, startingMinute);
        startTime1.set(Calendar.HOUR_OF_DAY, range1.getStartingHour());
        startTime1.set(Calendar.MINUTE, range1.getStartingMinute());
        
        return startTime0.before(startTime1);
    }
    
    public int getStartingHour() {
        return startingHour;
    }
    
    public int getStartingMinute() {
        return startingMinute;
    }
    
    public int getEndingHour() {
        return endingHour;
    }
    
    public int getEndingMinute() {
        return endingMinute;
    }
    
    @Override
    public String toString() {
        return String.format("%1$02d:%2$02d-%3$02d:%4$02d", startingHour, startingMinute, endingHour, endingMinute);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ( !(o instanceof HourRange) ) return false;
        
        HourRange another = (HourRange) o;
        return ( (this.startingHour == another.getStartingHour()) &&
                (this.startingMinute == another.getStartingMinute()) &&
                (this.endingHour == another.getEndingHour()) &&
                (this.endingMinute == another.getEndingMinute()));
    }

    @Override
    public int compareTo(HourRange another) {
        if (this.equals(another)) return 0;
        if (another.after(this)) return -1;
        return 1;
    }
}
