package org.yaoha;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.util.Log;

public class YaohaMapListener implements MapListener, OsmNodeRetrieverListener {
    YaohaMapActivity mapActivity;
    GeoPoint mapCenter;
    int zoomLevel;
    BoundingBoxE6 boundingBox;
    Boolean updateAmenities;
    
    public YaohaMapListener(YaohaMapActivity mapContext) {
        this.mapActivity = mapContext;
        mapCenter = new GeoPoint(0, 0);
        zoomLevel = 0;
        updateAmenities = false;
    }

    @Override
    public boolean onScroll(ScrollEvent arg0) {
        GeoPoint newMapCenter = (GeoPoint) arg0.getSource().getMapCenter();
        int latDiff = Math.abs(newMapCenter.getLatitudeE6() - mapCenter.getLatitudeE6());
        int lonDiff = Math.abs(newMapCenter.getLongitudeE6() - mapCenter.getLongitudeE6());
        if (latDiff > 1000 || lonDiff > 1000) {
            mapCenter = newMapCenter;
            if (updateAmenities) {
                update(arg0.getSource().getBoundingBox());
            }
            boundingBox = arg0.getSource().getBoundingBox();

            return true;
        }
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        int newZoomLevel = event.getZoomLevel();
        if (newZoomLevel != this.zoomLevel) {
            if (newZoomLevel >= 13) {
                updateAmenities = true;
                if(zoomLevel < 13) update(event.getSource().getBoundingBox());
            }
            else {
                updateAmenities = false;
            }
            zoomLevel = newZoomLevel;
            boundingBox = event.getSource().getBoundingBox();

            return true;
        }
        return false;
    }
    
    private void update(BoundingBoxE6 bbox) {
        String latLow = "";
        String latHigh = "";
        String lonLow = "";
        String lonHigh = "";
        if ( (bbox.getLatNorthE6() == bbox.getLatSouthE6()) || (bbox.getLonEastE6() == bbox.getLonWestE6()) ) return;
        
        if (bbox.getLatNorthE6() > bbox.getLatSouthE6()) {
            latHigh = Double.toString(bbox.getLatNorthE6()/1000000.0);
            latLow = Double.toString(bbox.getLatSouthE6()/1000000.0);
        }
        else {
            latLow =Double.toString( bbox.getLatNorthE6()/1000000.0);
            latHigh = Double.toString(bbox.getLatSouthE6()/1000000.0);
        }
        if (bbox.getLonEastE6() > bbox.getLonWestE6()) {
            lonHigh = Double.toString(bbox.getLonEastE6()/1000000.0);
            lonLow = Double.toString(bbox.getLonWestE6()/1000000.0);
        }
        else {
            lonLow = Double.toString(bbox.getLonEastE6()/1000000.0);
            lonHigh = Double.toString(bbox.getLonWestE6()/1000000.0);
        }
        
        latLow = latLow.replace(',', '.');
        latHigh = latHigh.replace(',', '.');
        lonLow = lonLow.replace(',', '.');
        lonHigh = lonHigh.replace(',', '.');
        
        OsmNodeRetrieverTask task = new OsmNodeRetrieverTask();
        task.addListener(this);
        String requestString = "node[bbox=" + lonLow + "," + latLow + "," + lonHigh + "," + latHigh + "][opening_hours=*]";
        task.execute(requestString);
        
        Log.d("YaohaMapListener", "Updated; request String: " + requestString);
        /*
        if (this.boundingBox == null) {
            this.mapActivity.updateShops(bbox.getLatNorthE6(), bbox.getLatSouthE6(), bbox.getLonEastE6(), bbox.getLonWestE6());
        } else {
            // TODO the user moved the map, only retrieve the shops for the new map content
        }
        */
    }

    @Override
    public void onRequestComplete() {
        Log.d("YaohaMapListener", "callback called");
        Log.d("YaohaMapListener", "There are " + Nodes.getInstance().getNodeMap().size() + " nodes in the nodeMap");
        // Draw nodes in map
    }

}
// min zoom lvl 13