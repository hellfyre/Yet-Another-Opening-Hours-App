package org.yaoha;

import java.util.Date;
import java.util.HashMap;

public class OSMNode {
    private int ID;
    private int latitudeE6;
    private int longitudeE6;
    private String name;
    private String amenity;
    private String opening_hours;
    private Date lastUpdated;
    private HashMap<String, String> attributes;
    
    public OSMNode(String ID, String latitude, String longitude) {
        this.ID = Integer.parseInt(ID);
        this.latitudeE6 = new Double(Double.parseDouble(latitude)).intValue()*1000000;
        this.longitudeE6 = new Double(Double.parseDouble(longitude)).intValue()*1000000;
    }
    
    public OSMNode(int ID, int latitudeE6, int longitudeE6) {
        this.ID = ID;
        this.latitudeE6 = latitudeE6;
        this.longitudeE6 = longitudeE6;
    }
    
    public void putAttribute(String key, String value) {
        attributes.put(key, value);
    }
    
    public String getAttribute(String key) {
        return attributes.get(key);
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmenity() {
        return amenity;
    }

    public void setAmenity(String amenity) {
        this.amenity = amenity;
    }

    public String getOpening_hours() {
        return opening_hours;
    }

    public void setOpening_hours(String opening_hours) {
        this.opening_hours = opening_hours;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
}
