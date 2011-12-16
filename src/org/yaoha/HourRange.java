package org.yaoha;

public class HourRange {
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
        return String.valueOf(startingHour) + ":" + String.valueOf(startingMinute) + "-" + String.valueOf(endingHour) + ":" + String.valueOf(endingMinute);
    }
}
