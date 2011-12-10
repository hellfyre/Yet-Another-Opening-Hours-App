package org.yaoha;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class OsmXmlParser {
    HashMap<Integer, OsmNode> nodeMap;
    SAXParser parser;

    public OsmXmlParser() {
        nodeMap = new HashMap<Integer, OsmNode>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public HashMap<Integer, OsmNode> getNodes() {
        return nodeMap;
    }
    
    public void parse(InputStream in) {
        try {
            parser.parse(in, new OsmXmlHandler(nodeMap));
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO: write in ContentProvider
    }

}
