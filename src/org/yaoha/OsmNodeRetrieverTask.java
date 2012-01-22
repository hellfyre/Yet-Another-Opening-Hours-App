package org.yaoha;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import android.os.AsyncTask;
import android.util.Log;

public class OsmNodeRetrieverTask extends AsyncTask<Void, Void, Void> {
    private ArrayList<OsmNodeRetrieverListener> receiverList = new ArrayList<OsmNodeRetrieverListener>();
    private SimpleQueue<URI> queue = new SimpleQueue<URI>();
    
    public OsmNodeRetrieverTask(URI uri) {
        queue.add(uri);
    }

    @Override
    protected Void doInBackground(Void... params) {
        
        while (!queue.isEmpty()) {
            URI uri = null;
            synchronized (queue) {
                uri = queue.remove();
            }
            Log.d(OsmNodeRetrieverTask.class.getSimpleName(), "Queue holds " + queue.size() + " tasks");
            ApiConnector connector = new ApiConnector();
            InputStream in;
            try {
                in = connector.getNodes(uri);
            } catch (ClientProtocolException e) {
                Log.d(OsmNodeRetrieverTask.class.getSimpleName(), e.getMessage());
                continue;
            } catch (IOException e) {
                Log.d(OsmNodeRetrieverTask.class.getSimpleName(), e.getMessage());
                continue;
            }
            
            OsmXmlParser parser = new OsmXmlParser();
            parser.parse(in, OsmNodeDbHelper.getInstance());
        }
        
        callListenersAllRequestsDone();
        
        return null;
    }
    
    public void addTask(URI uri) {
        synchronized (queue) {
            queue.add(uri);
        }
        Log.d(OsmNodeRetrieverTask.class.getSimpleName(), "Queue holds " + queue.size() + " tasks");
    }
    
    public void addListener(OsmNodeRetrieverListener receiver) {
        receiverList.add(receiver);
    }
    
    private void callListenersAllRequestsDone() {
        for (OsmNodeRetrieverListener receiver : receiverList) {
            receiver.onAllRequestsProcessed();
        }
    }

}
