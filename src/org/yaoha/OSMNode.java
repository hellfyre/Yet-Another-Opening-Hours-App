package org.yaoha;

import java.util.HashMap;

public class OSMNode {
    private int ID;
    private int latitudeE6;
    private int longitudeE6;
    private HashMap<String, String> attributes;
    
    public OSMNode(int ID, int latitudeE6, int longitudeE6) {
        this.ID = ID;
        this.latitudeE6 = latitudeE6;
        this.longitudeE6 = longitudeE6;
    }
    
    public OSMNode(String ID, String latitude, String longitude) {
        this.ID = Integer.parseInt(ID);
        this.latitudeE6 = new Double(Double.parseDouble(latitude)).intValue()*1000000;
        this.longitudeE6 = new Double(Double.parseDouble(longitude)).intValue()*1000000;
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
    
}
