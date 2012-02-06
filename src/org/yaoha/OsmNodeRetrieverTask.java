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
    private NodeReceiverInterface<OsmNode> nodeReceiver;
    
    public OsmNodeRetrieverTask(NodeReceiverInterface<OsmNode> nodeReceiver) {
        this.nodeReceiver = nodeReceiver;
    }
    
    public OsmNodeRetrieverTask() {
        this(OsmNodeDbHelper.getInstance());
    }
    
    public OsmNodeRetrieverTask(URI uri) {
        this();
        queue.add(uri);
    }
    
    public OsmNodeRetrieverTask(URI uri, NodeReceiverInterface<OsmNode> nodeReceiver) {
        this(nodeReceiver);
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
                in = connector.getNodes(uri).getEntity().getContent();
            } catch (ClientProtocolException e) {
                Log.d(OsmNodeRetrieverTask.class.getSimpleName(), e.getMessage());
                continue;
            } catch (IOException e) {
                Log.d(OsmNodeRetrieverTask.class.getSimpleName(), e.getMessage());
                continue;
            }
            
            OsmXmlParser parser = new OsmXmlParser();
            parser.parse(in, this.nodeReceiver);
        }
        
        callListenersAllRequestsDone();
        
        return null;
    }
    
    public void addTask(URI uri) {
        Log.d(OsmNodeRetrieverTask.class.getSimpleName(), "Added request: " + uri.toString());
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
