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
            //Toast.makeText(mapContext, "Scrolled to " + mapCenter.getLatitudeE6()/1000000.0 + ", " + mapCenter.getLongitudeE6()/1000000.0 + ", " + zoomLevel, Toast.LENGTH_SHORT).show();
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
            //Toast.makeText(mapContext, "Zoomed to " + mapCenter.getLatitudeE6()/1000000.0 + ", " + mapCenter.getLongitudeE6()/1000000.0 + ", " + zoomLevel, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    
    private void update(BoundingBoxE6 bbox) {
        String toastTxt = "Update! Center: " + mapCenter.getLatitudeE6()/1000000.0 + ", " + mapCenter.getLongitudeE6()/1000000.0 + ", " + zoomLevel + "\n" +
                "Bb lat N S " + boundingBox.getLatNorthE6()/1000000.0 + ", " + boundingBox.getLatSouthE6()/1000000.0 + "\n" +
                "Bb lon E W " + boundingBox.getLonEastE6()/1000000.0 + ", " + boundingBox.getLonWestE6()/1000000.0;
        Toast.makeText(mapActivity, toastTxt, Toast.LENGTH_LONG).show();
        
        if (this.boundingBox == null) {
            this.mapActivity.updateShops(bbox.getLatNorthE6(), bbox.getLatSouthE6(), bbox.getLonEastE6(), bbox.getLonWestE6());
        } else {
            // TODO the user moved the map, only retrieve the shops for the new map content
        }
    }

    @Override
    public void onRequestComplete() {
        Log.d("YaohaMapListener", "callback called");
        Log.d("YaohaMapListener", "There are " + Nodes.getInstance().getNodeMap().size() + " nodes in the nodeMap");
        // Draw nodes in map
    }

}
// min zoom lvl 13