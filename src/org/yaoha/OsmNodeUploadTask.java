package org.yaoha;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import android.os.AsyncTask;

public class OsmNodeUploadTask extends AsyncTask<OsmNode, Void, String> {
    OsmNode currentNode;
    ApiConnector connector;
    String changesetId;
    OsmNodeUploadListener receiver;

    @Override
    protected String doInBackground(OsmNode... params) {
        if (params.length > 1) return "You must not start this task with more than one node";
        this.currentNode = params[0];
        connector = new ApiConnector();
        
        HttpResponse createResponse = null;
        try {
            createResponse = connector.createNewChangeset();
        } catch (Exception e) {
            // ClientProtocolException
            // IOException
            return "Creation of changeset failed";
        }
        if (createResponse.getStatusLine().getStatusCode() != 200) {
            if (createResponse.getStatusLine().getStatusCode() == 401) {
                return "Authentication failed";
            }
            return "Creation of changeset failed: Status " + createResponse.getStatusLine().getStatusCode();
        }
        
        try {
            changesetId = inputStreamToString(createResponse.getEntity().getContent());
        } catch (Exception e) {
            return "Something went horribly wrong! Call your doctor, pack your bags and leave town!";
        }
        
        HttpResponse uploadResponse = null;
        try {
            uploadResponse = connector.putNode(changesetId, currentNode);
        } catch (ClientProtocolException e) {
            return "Uploading node failed";
        } catch (IOException e) {
            return "Uploading node failed";
        } catch (ParserConfigurationException e) {
            return "Uploading node failed: Couldn't serialize node object";
        } catch (TransformerException e) {
            return "Uploading node failed: Couldn't serialize node object";
        }
        
        if (uploadResponse.getStatusLine().getStatusCode() != 200) {
            String uploadResponseString = "";
            try {
                uploadResponseString = inputStreamToString(uploadResponse.getEntity().getContent());
            } catch (Exception e) {
                return "Exception";
            }
            return "Uploading node failed: " + uploadResponseString;
        }
        
        try {
            connector.closeChangeset(changesetId);
        } catch (Exception e) {
            // ClientProtocolException
            // IOException
            return "Creation of changeset failed";
        }
        
        return "Successfully uploaded node";
    }
    
    public void addReceiver(OsmNodeUploadListener receiver) {
        // There'll be only one receiver anyhow
        if (this.receiver == null) {
            this.receiver = receiver;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        receiver.onNodeUploaded(result);
    }

    private String inputStreamToString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String returnString = "";
        String line = "";
        while ((line = reader.readLine()) != null) {
            returnString += line;
        }
        return returnString;
    }

}
