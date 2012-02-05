package org.yaoha;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class OsmNode {
    public enum shopStatus {OPEN, CLOSED, UNSET, MAYBE, PARSERERROR};
    private int ID;
    private int latitudeE6;
    private int longitudeE6;
    private int version;
    private Date lastUpdated;
    private HashMap<String, String> attributes;
    private OpeningHours openingHours = null;
    
    public OsmNode(String ID, String latitude, String longitude, String version) {
        this.ID = Integer.parseInt(ID);
        this.latitudeE6 = new Double(Double.parseDouble(latitude)*1e6).intValue();
        this.longitudeE6 = new Double(Double.parseDouble(longitude)*1e6).intValue();
        if (version.equals("")) {
            this.version = 0;
        }
        else {
            this.version = Integer.parseInt(version);
        }
        this.attributes = new HashMap<String, String>();
    }
    
    public OsmNode(int ID, int latitudeE6, int longitudeE6, int version) {
        this.ID = ID;
        this.latitudeE6 = latitudeE6;
        this.longitudeE6 = longitudeE6;
        this.version = version;
        this.attributes = new HashMap<String, String>();
    }
    
    /**
     * Assumes that changes in opening_hours are already saved to attributes
     * @throws ParserConfigurationException, TransformerException 
     */
    public String serialize(String changesetId) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        
        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("osm");
        Element nodeElement = doc.createElement("node");
        rootElement.appendChild(nodeElement);
        doc.appendChild(rootElement);
        
        nodeElement.setAttribute("changeset", changesetId);
        nodeElement.setAttribute("id", "" + this.ID);
        
        String latitudeString = String.format(Locale.US, "%f", this.latitudeE6/1e6);
        String longitudeString = String.format(Locale.US, "%f", this.longitudeE6/1e6);
        
        nodeElement.setAttribute("lat", latitudeString);
        nodeElement.setAttribute("lon", longitudeString);
        nodeElement.setAttribute("version", String.valueOf(this.version));
        
        Set<String> ts = new TreeSet<String>(attributes.keySet());
        for (String key : ts) {
            String value = this.attributes.get(key);
            Element tag_element = doc.createElement("tag");
            tag_element.setAttribute("k", key);
            tag_element.setAttribute("v", value);
            nodeElement.appendChild(tag_element);
        }
        
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
 
        transformer.transform(source, result);
        
        return writer.toString();
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
    
    public int getVersion() {
        return this.version;
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
        if (openingHours == null) {
            openingHours = new OpeningHours();
            parseOpeningHours();
        }
        return openingHours;
    }
    
    void commitOpeningHours() {
        setOpening_hours(getPointerToOpeningHours().compileOpeningHoursString());
    }
    
    public void parseOpeningHours() {
        openingHours.parse(getOpening_hours());
    }

    public shopStatus isOpenNow() {
        getPointerToOpeningHours();
        if (openingHours.hasParsingFailed())
            return shopStatus.PARSERERROR;
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
