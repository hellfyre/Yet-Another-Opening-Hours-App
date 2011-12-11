package org.yaoha;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

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
            return true;
        }
        return false;
    }
    
    private void update(BoundingBoxE6 bbox) {
        if ( (bbox.getLatNorthE6() == bbox.getLatSouthE6()) || (bbox.getLonEastE6() == bbox.getLonWestE6()) )
            return;
        
        // check for intersection between old and new bounding box
        if (this.boundingBox != null 
                && (this.boundingBox.getLatNorthE6() < bbox.getLatSouthE6() 
                        || this.boundingBox.getLatSouthE6() > bbox.getLatNorthE6()
                        || this.boundingBox.getLonEastE6() < bbox.getLonWestE6()
                        || this.boundingBox.getLonWestE6() > bbox.getLonEastE6())
                        ) {
            // no intersection found
            this.boundingBox = null;
        }
        
        if (this.boundingBox == null) {
            queryShopsInRectangle(bbox.getLatNorthE6(), bbox.getLatSouthE6(), bbox.getLonEastE6(), bbox.getLonWestE6());
        } else {
            // bounding box moved
            // assumes zooming out, too
            int northdiff = this.boundingBox.getLatNorthE6() - bbox.getLatNorthE6();
            int southdiff = this.boundingBox.getLatSouthE6() - bbox.getLatSouthE6();
            int eastdiff = this.boundingBox.getLonEastE6() - bbox.getLonEastE6();
            int westdiff = this.boundingBox.getLonWestE6() - bbox.getLonWestE6();
            
            // moved north
            if (northdiff < 0) {
                queryShopsInRectangle(bbox.getLatNorthE6(), this.boundingBox.getLatNorthE6(), this.boundingBox.getLonWestE6(), this.boundingBox.getLonEastE6());
            }
            // moved south
            if (southdiff > 0) {
                queryShopsInRectangle(this.boundingBox.getLatSouthE6(), bbox.getLatSouthE6(), this.boundingBox.getLonWestE6(), this.boundingBox.getLonEastE6());
            }
            // with latitude of bbox to get the entire height
            // moved east
            if (eastdiff < 0) {
                queryShopsInRectangle(bbox.getLatNorthE6(), bbox.getLatSouthE6(), bbox.getLonEastE6(), this.boundingBox.getLonEastE6());
            }
            // moved west
            if (westdiff > 0) {
                queryShopsInRectangle(bbox.getLatNorthE6(), bbox.getLatSouthE6(), this.boundingBox.getLonWestE6(), bbox.getLonWestE6());
            }

        }
        
        this.boundingBox = bbox;
    }
    
    void queryShopsInRectangle(double latHigh, double latLow, double lonHigh, double lonLow) {
        if (latHigh < latLow) {
            double tmp = latLow;
            latLow = latHigh;
            latHigh = tmp;
        }
        
        if (lonHigh < lonLow) {
            double tmp = lonLow;
            lonLow = lonHigh;
            lonHigh = tmp;
        }
        
        String latLows = Double.toString(latLow/1e6);
        String latHighs = Double.toString(latHigh/1e6);
        String lonLows = Double.toString(lonLow/1e6);
        String lonHighs = Double.toString(lonHigh/1e6);
        
        latLows = latLows.replace(',', '.');
        latHighs = latHighs.replace(',', '.');
        lonLows = lonLows.replace(',', '.');
        lonHighs = lonHighs.replace(',', '.');

        // TODO query for [shop=*] or [amenity=*] too
        OsmNodeRetrieverTask task = new OsmNodeRetrieverTask();
        task.addListener(this);
        String requestString = "node[bbox=" + lonLows + "," + latLows + "," + lonHighs + "," + latHighs + "][opening_hours=*]";
        task.execute(requestString);
        
        Log.d("YaohaMapListener", "Updated; request String: " + requestString);
    }
    
    @Override
    public void onRequestComplete() {
        Log.d("YaohaMapListener", "callback called");
        Log.d("YaohaMapListener", "There are " + Nodes.getInstance().getNodeMap().size() + " nodes in the nodeMap");
        
        // Draw nodes in map
        MapView mv = (MapView) mapActivity.findViewById(R.id.mapview);
        mv.postInvalidate();
    }

}
// min zoom lvl 13