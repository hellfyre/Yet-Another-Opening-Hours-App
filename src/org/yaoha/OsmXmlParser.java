package org.yaoha;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class OsmXmlParser {
    ArrayList<OsmNode> nodeList;
    SAXParser parser;

    public OsmXmlParser() {
        nodeList = new ArrayList<OsmNode>();
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
    
    public ArrayList<OsmNode> getNodeList() {
        return nodeList;
    }
    
    public void parse(InputStream in) {
        try {
            parser.parse(in, new OsmXmlHandler(nodeList));
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
