package org.yaoha;

import java.io.InputStream;

import android.app.IntentService;
import android.content.Intent;

public class OsmNodeRetrieverService extends IntentService {

    public OsmNodeRetrieverService() {
        super("updateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String requestString = intent.getExtras().getString("request");
        
        OverpassConnector connector = new OverpassConnector();
        InputStream in = connector.getResponseInputStream(requestString);
        
        OsmXmlParser parser = new OsmXmlParser();
        parser.parse(in);
        
    }
    
    // TODO: write Listener to notify MapView

}
