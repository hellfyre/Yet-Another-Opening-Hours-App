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
    protected OpeningHours openingHours;

    public OsmNode(String ID, String latitude, String longitude, String version) {
        try {
            this.ID = Integer.parseInt(ID);
        } catch (NumberFormatException ne) {
            this.ID = -1;
            ne.printStackTrace();
        }    
        this.latitudeE6 = Double.valueOf(Double.parseDouble(latitude)*1e6).intValue();
        this.longitudeE6 = Double.valueOf(Double.parseDouble(longitude)*1e6).intValue();


        if (version.equals("")) {
            this.version = 0;
        }
        else {
            this.version = Integer.parseInt(version);
        }
        this.attributes = new HashMap<String, String>();
        openingHours = new OpeningHours();
    }

    public OsmNode(int ID, int latitudeE6, int longitudeE6, int version) {
        try {
            this.ID = ID;
        } catch (NumberFormatException ne) {
            this.ID = -1;
            ne.printStackTrace();
        }
        this.latitudeE6 = latitudeE6;
        this.longitudeE6 = longitudeE6;
        this.version = version;
        this.attributes = new HashMap<String, String>();
        openingHours = new OpeningHours();
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
        if (key.equals("opening_hours")) parseOpeningHours();
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

    public String getOpeningHoursString() {
        return getAttribute("opening_hours");
    }

    public void setOpeningHoursString(String opening_hours) {
        putAttribute("opening_hours", opening_hours);
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void commitOpeningHours() {
        if (openingHours == null) return;
        setOpeningHoursString(openingHours.compileOpeningHoursString());
    }

    public void parseOpeningHours() {
        openingHours.parse(getOpeningHoursString());
    }

    public shopStatus isOpenNow() {
        if (openingHours.isEmpty())
            return shopStatus.UNSET;
        if (openingHours.unparsable())
            return shopStatus.PARSERERROR;

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

        TreeSet<HourRange> today = openingHours.getWeekDay(todayIndex);

        if (today.isEmpty())
            return shopStatus.CLOSED;

        for (HourRange curRange : today) {
            int nowHour = now.get(Calendar.HOUR_OF_DAY);
            int nowMinute = now.get(Calendar.MINUTE);
            
            //workaround for opening_hours like 13:00 - 2:00 (will only show shops open until 24:00)
            //TODO look for better solution
            int tmpEndingHour = curRange.getEndingHour();
            if (curRange.getEndingHour() < curRange.getStartingHour()){
                tmpEndingHour = 24;                
            }
            
            if (nowHour >= curRange.getStartingHour() && nowMinute >= curRange.getStartingMinute()) {
                if (nowHour <= curRange.getEndingHour() && nowMinute <= curRange.getEndingMinute()) {
                    result = shopStatus.OPEN;
                }
                if (nowHour > curRange.getStartingHour() || (nowHour == curRange.getStartingHour() && nowMinute >= curRange.getStartingMinute())) {
                    if (nowHour < tmpEndingHour || (nowHour == tmpEndingHour && nowMinute <= curRange.getEndingMinute())) {

                        result = shopStatus.OPEN;
                    }    
                }
                else if (curRange.getEndingHour() == -1) {
                    result = shopStatus.MAYBE;
                }
            }
        }
        return result;
    }

}
