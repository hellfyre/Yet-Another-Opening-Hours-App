package org.yaoha;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OsmXmlHandler extends DefaultHandler {
    ArrayList<OsmNode> nodeList;
    OsmNode currentNode;
    String timestamp;
    String parentElement;
    
    public OsmXmlHandler(ArrayList<OsmNode> nodeList) {
        this.nodeList = nodeList;
        currentNode = null;
        timestamp = null;
        parentElement = null;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (qName.equals("meta")) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).equals("osm_base")) timestamp = attributes.getValue(0);
            }
        }
        else if (qName.equals("node")) {
            if (parentElement == null) {
                parentElement = qName;
                String id = "";
                String latitude = "";
                String longitude = "";
                for (int i=0; i < attributes.getLength(); i++) {
                    if (attributes.getQName(i).equals("id")) id = attributes.getValue(i);
                    else if (attributes.getQName(i).equals("lat")) latitude = attributes.getValue(i);
                    else if (attributes.getQName(i).equals("lon")) longitude = attributes.getValue(i);
                }
                currentNode = new OsmNode(id, latitude, longitude);
                if (timestamp != null) {
                    while (timestamp.contains("\\")) {
                        timestamp = timestamp.replace("\\", "");
                    }
                    timestamp = timestamp.replace("Z", "+0000");
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
                    try {
                        currentNode.setLastUpdated(df.parse(timestamp));
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        else if (qName.equals("tag")) {
            if (parentElement.equals("node") && currentNode != null) {
                int keyIndex = -1;
                int valueIndex = -1;
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (attributes.getQName(i).equals("k")) keyIndex = i;
                    else if (attributes.getQName(i).equals("v")) valueIndex = i;
                }
                String key = attributes.getValue(keyIndex);
                String value = attributes.getValue(valueIndex);
                
                if (key.equals("name")) currentNode.setName(value);
                else if (key.equals("amenity")) currentNode.setAmenity(value);
                else if (key.equals("opening_hours")) currentNode.setOpening_hours(value);
                else currentNode.putAttribute(key, value);
            }
        }
        super.startElement(uri, localName, qName, attributes);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("node") && (currentNode != null) && parentElement.equals("node")) {
            nodeList.add(currentNode);
            currentNode = null;
            parentElement = null;
        }
        super.endElement(uri, localName, qName);
    }
}
