package org.yaoha;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.AsyncTask;

public class OsmNodeRetrieverTask extends AsyncTask<String, Void, Void> {
    private ArrayList<OsmNodeRetrieverListener> receiverList = new ArrayList<OsmNodeRetrieverListener>();

    @Override
    protected Void doInBackground(String... params) {
        String requestString = "";
        if (params.length == 1) {
            requestString = params[0];
        }
        
        OverpassConnector connector = new OverpassConnector();
        InputStream in = connector.getResponseInputStream(requestString);
        
        OsmXmlParser parser = new OsmXmlParser();
        parser.parse(in, Nodes.getInstance().getNodeMap());
        
        // Parse opening_hours
        HashMap<Integer, OsmNode> nodes = Nodes.getInstance().getNodeMap();
        for (Integer i : nodes.keySet()) {
            OsmNode node = nodes.get(i);
            node.parseOpeningHours();
        }
        
        callListeners();
        
        return null;
    }
    
    public void addListener(OsmNodeRetrieverListener receiver) {
        receiverList.add(receiver);
    }
    
    private void callListeners() {
        for (OsmNodeRetrieverListener receiver : receiverList) {
            receiver.onRequestComplete();
        }
    }

}
