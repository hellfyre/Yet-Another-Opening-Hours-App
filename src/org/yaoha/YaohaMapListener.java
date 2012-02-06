/*
 *  Copyright 2012 Stefan Hobohm, Lutz Reinhardt, Matthias Uschok
 *
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
 */

package org.yaoha;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    OsmNodeRetrieverTask retrieverTask = null;
    NodesOverlay no;
    enum Direction {NORTH, SOUTH, WEST, EAST};
    static final float mapOversize = 0.1f;
    
    public YaohaMapListener(YaohaMapActivity mapContext, NodesOverlay no) {
        this.mapActivity = mapContext;
        mapCenter = new GeoPoint(0, 0);
        zoomLevel = 0;
        updateAmenities = false;
        this.no = no;
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
    
    
    static Map<Direction, Boolean> getBoundingBoxMove(BoundingBoxE6 old_bb, BoundingBoxE6 new_bb) {
        // check for intersection between old and new bounding box
        if (old_bb == null 
                || (old_bb.getLatNorthE6() < new_bb.getLatSouthE6() 
                        || old_bb.getLatSouthE6() > new_bb.getLatNorthE6()
                        || old_bb.getLonEastE6() < new_bb.getLonWestE6()
                        || old_bb.getLonWestE6() > new_bb.getLonEastE6())
                        ) {
            // no intersection found
            return null;
        }
        Map<Direction, Boolean> ret_val = new HashMap<YaohaMapListener.Direction, Boolean>(4);
        ret_val.put(Direction.NORTH, old_bb.getLatNorthE6() - new_bb.getLatNorthE6() < 0);
        ret_val.put(Direction.SOUTH, old_bb.getLatSouthE6() - new_bb.getLatSouthE6() > 0);
        ret_val.put(Direction.EAST, old_bb.getLonEastE6() - new_bb.getLonEastE6() < 0);
        ret_val.put(Direction.WEST, old_bb.getLonWestE6() - new_bb.getLonWestE6() > 0);
        return ret_val;
    }
    
    static boolean contains(BoundingBoxE6 bigger, BoundingBoxE6 smaller) {
        return bigger.contains(smaller.getLatNorthE6(), smaller.getLonWestE6())
        && bigger.contains(smaller.getLatNorthE6(), smaller.getLonEastE6())
        && bigger.contains(smaller.getLatSouthE6(), smaller.getLonWestE6())
        && bigger.contains(smaller.getLatSouthE6(), smaller.getLonEastE6());
    }
    
    void update(BoundingBoxE6 bbox) {
        if ( (bbox.getLatNorthE6() == bbox.getLatSouthE6()) || (bbox.getLonEastE6() == bbox.getLonWestE6()) )
            return;
        
        // check if bbox is inside boundingBox
        if (boundingBox != null && contains(boundingBox, bbox)) {
            return;
        }
        
        boundingBox = bbox.increaseByScale(2 + mapOversize);
        
        no.getNodes(boundingBox);
        
        queryShopsInRectangle(boundingBox.getLatNorthE6(),
                boundingBox.getLatSouthE6(),
                boundingBox.getLonEastE6(),
                boundingBox.getLonWestE6());
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
        
        String latLows = String.format(Locale.US, "%f", latLow/1e6);
        String latHighs = String.format(Locale.US, "%f", latHigh/1e6);
        String lonLows = String.format(Locale.US, "%f", lonLow/1e6);
        String lonHighs = String.format(Locale.US, "%f", lonHigh/1e6);

        // TODO query for [shop=*] or [amenity=*] too
        List<URI> requestUris = ApiConnector.getRequestUriXapi(lonLows, latLows, lonHighs, latHighs, null, null, null, this.mapActivity.editMode);
        if (requestUris.size() == 0) return;
        if (retrieverTask != null) {
            for (URI uri : requestUris)
                retrieverTask.addTask(uri);
        }
        else {
            retrieverTask = new OsmNodeRetrieverTask();
            for (URI uri : requestUris)
                retrieverTask.addTask(uri);
            retrieverTask.addListener(this);
            retrieverTask.execute();
        }
        
        Log.d(YaohaMapListener.class.getSimpleName(), "Update triggered: " + requestUris.toString());
    }
    
    @Override
    public void onAllRequestsProcessed() {
        retrieverTask = null;
        Log.d(YaohaMapListener.class.getSimpleName(), "Retriever task released");
    }

    public void requeryBoundingBox() {
        BoundingBoxE6 tmp_bb = this.boundingBox;
        this.boundingBox = null;
        if (tmp_bb != null)
            this.update(tmp_bb);
    }

}
