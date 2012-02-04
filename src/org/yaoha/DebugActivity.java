package org.yaoha;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DebugActivity extends Activity implements OnClickListener, NodeReceiverInterface<OsmNode> {
    ApiConnector connector = new ApiConnector();
    String changesetId = "";
    OsmNode currentNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_view);
        
        Button buttonGetNode = (Button) findViewById(R.id.debugButtonGetNode);
        Button buttonCreateChangeset = (Button) findViewById(R.id.debugButtonCreateChangeset);
        Button buttonUploadNode = (Button) findViewById(R.id.debugButtonUploadNode);
        Button buttonCloseChangeset = (Button) findViewById(R.id.debugButtonCloseChangeset);
        
        buttonGetNode.setOnClickListener(this);
        buttonCreateChangeset.setOnClickListener(this);
        buttonUploadNode.setOnClickListener(this);
        buttonCloseChangeset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EditText output = (EditText) findViewById(R.id.debugEditOutput);
        switch (v.getId()) {
        case R.id.debugButtonGetNode:
            InputStream response = null;
            try {
                response = connector.getNodes(ApiConnector.getRequestUriDevApiGetNode("270066849"));
            } catch (ClientProtocolException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            OsmXmlParser parser = new OsmXmlParser();
            parser.parse(response, this);
            output.append("---------- get node ----------" + "\n");
            try {
                output.append(currentNode.serialize("NULL") + "\n");
            } catch (ParserConfigurationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (TransformerException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            break;
        case R.id.debugButtonCreateChangeset:
            try {
                changesetId = inputStreamToString(connector.createNewChangeset());
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            output.append("---------- create changeset ----------" + "\n");
            output.append(changesetId + "\n");
            break;
        case R.id.debugButtonUploadNode:
            String updatedNode = "";
            currentNode.setName(currentNode.getName() + " foobar");
            try {
                updatedNode = inputStreamToString(connector.uploadNode(ApiConnector.getRequestUriDevApiUpdateNode(String.valueOf(currentNode.getID())), changesetId, currentNode));
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            output.append("---------- upload node ----------" + "\n");
            output.append(updatedNode + "\n");
            break;
        case R.id.debugButtonCloseChangeset:
            String closeResponse = "";
            try {
                closeResponse = inputStreamToString(connector.closeChangeset(changesetId));
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            output.append("---------- close changeset ----------" + "\n");
            output.append(closeResponse + "\n");
            break;
        default:
            return;
        }
    }
    
    private String inputStreamToString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String returnString = "";
        String line = "";
        while ( (line = reader.readLine()) != null ) {
            returnString += line;
        }
        return returnString;
    }

    @Override
    public void put(OsmNode value) {
        this.currentNode = value;
    }
}
