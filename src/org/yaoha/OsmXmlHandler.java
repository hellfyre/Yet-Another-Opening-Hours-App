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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OsmXmlHandler extends DefaultHandler {
    NodeReceiverInterface<OsmNode> nodeMap;
    OsmNode currentNode;
    String timestamp;
    String parentElement;
    
    public OsmXmlHandler(NodeReceiverInterface<OsmNode> nodeList) {
        this.nodeMap = nodeList;
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
                String version = "";
                for (int i=0; i < attributes.getLength(); i++) {
                    if (attributes.getQName(i).equals("id")) id = attributes.getValue(i);
                    else if (attributes.getQName(i).equals("lat")) latitude = attributes.getValue(i);
                    else if (attributes.getQName(i).equals("lon")) longitude = attributes.getValue(i);
                    else if (attributes.getQName(i).equals("version")) version = attributes.getValue(i);
                }
                currentNode = new OsmNode(id, latitude, longitude, version);
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
                
                currentNode.putAttribute(key, value);
                if (key.equals("opening_hours")) currentNode.parseOpeningHours();
            }
        }
        super.startElement(uri, localName, qName, attributes);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("node") && (currentNode != null) && parentElement.equals("node")) {
            nodeMap.put(currentNode);
            currentNode = null;
            parentElement = null;
        }
        super.endElement(uri, localName, qName);
    }
}
