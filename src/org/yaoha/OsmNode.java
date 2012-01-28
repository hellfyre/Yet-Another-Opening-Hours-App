package org.yaoha;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class OsmNode {
    public enum shopStatus {OPEN, CLOSED, UNSET, MAYBE};
    private int ID;
    private int latitudeE6;
    private int longitudeE6;
    private Date lastUpdated;
    private HashMap<String, String> attributes;
    private OpeningHours openingHours = new OpeningHours();
    
    public OsmNode(String ID, String latitude, String longitude) {
        this.ID = Integer.parseInt(ID);
        this.latitudeE6 = new Double(Double.parseDouble(latitude)*1e6).intValue();
        this.longitudeE6 = new Double(Double.parseDouble(longitude)*1e6).intValue();
        this.attributes = new HashMap<String, String>();
    }
    
    public OsmNode(int ID, int latitudeE6, int longitudeE6) {
        this.ID = ID;
        this.latitudeE6 = latitudeE6;
        this.longitudeE6 = longitudeE6;
        this.attributes = new HashMap<String, String>();
    }
    
    public void putAttribute(String key, String value) {
        attributes.put(key, value);
    }
    
    public String getAttribute(String key) {
        return attributes.get(key);
    }
    
    public Set<String> getKeys() {
        return attributes.keySet();
    }
    
    public int getID() {
        return ID;
    }
    
    public int getLatitudeE6() {
        return latitudeE6;
    }
    
    public int getLongitudeE6() {
        return longitudeE6;
    }

    public String getName() {
        return getAttribute("name");
    }

    public void setName(String name) {
        putAttribute("name", name);
    }

    public String getAmenity() {
        return getAttribute("amenity");
    }

    public void setAmenity(String amenity) {
        putAttribute("amenity", amenity);
    }

    public String getOpening_hours() {
        return getAttribute("opening_hours");
    }

    public void setOpening_hours(String opening_hours) {
        putAttribute("opening_hours", opening_hours);
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    protected OpeningHours getPointerToOpeningHours() {
        return openingHours;
    }
    
    public void parseOpeningHours() throws java.text.ParseException {
        openingHours.parse(getOpening_hours());
    }

    public shopStatus isOpenNow() {
        if (openingHours.isEmpty())
            return shopStatus.UNSET;
        
        shopStatus result = shopStatus.CLOSED;
        Calendar now = Calendar.getInstance();

        int todayIndex;
        switch (now.get(Calendar.DAY_OF_WEEK)) {
        case Calendar.MONDAY:
            todayIndex = OpeningHours.MONDAY;
            break;
        case Calendar.TUESDAY:
            todayIndex = OpeningHours.TUESDAY;
            break;
        case Calendar.WEDNESDAY:
            todayIndex = OpeningHours.WEDNESDAY;
            break;
        case Calendar.THURSDAY:
            todayIndex = OpeningHours.THURSDAY;
            break;
        case Calendar.FRIDAY:
            todayIndex = OpeningHours.FRIDAY;
            break;
        case Calendar.SATURDAY:
            todayIndex = OpeningHours.SATURDAY;
            break;
        case Calendar.SUNDAY:
            todayIndex = OpeningHours.SUNDAY;
            break;
        default:
            todayIndex = -1;
            break;
        }
        
        TreeSet<HourRange> today = openingHours.getDay(todayIndex);

        if (today.isEmpty())
            return shopStatus.CLOSED;

        for (HourRange curRange : today) {
            int nowHour = now.get(Calendar.HOUR_OF_DAY);
            int nowMinute = now.get(Calendar.MINUTE);
            if (nowHour > curRange.getStartingHour() || (nowHour == curRange.getStartingHour() && nowMinute >= curRange.getStartingMinute())) {
                if (nowHour < curRange.getEndingHour() || (nowHour == curRange.getEndingHour() && nowMinute <= curRange.getEndingMinute())) {
                    result = shopStatus.OPEN;
                }
                else if (curRange.getEndingHour() == -1) {
                    result = shopStatus.MAYBE;
                }
            }
        }
        return result;
    }

}
