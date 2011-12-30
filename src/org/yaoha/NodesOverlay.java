package org.yaoha;

import java.util.HashMap;
import java.util.Iterator;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class NodesOverlay extends ItemizedOverlay<OverlayItem> {
    final int offset = 20;
    HashMap<Integer, OsmNode> nodes;
    Iterator<Integer> iter;
    Activity act;
    
    public NodesOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy, Activity act) {
        super(pDefaultMarker, pResourceProxy);
        this.act = act;
        getNodes();
    }

    @SuppressWarnings("unchecked")
    void getNodes() {
        int old_size;
        if (nodes == null)
            old_size = 0;
        else
            old_size = nodes.size();
        if (nodes == null || nodes.size() != Nodes.getInstance().getNodeMap().size())
            nodes = (HashMap<Integer, OsmNode>)Nodes.getInstance().getNodeMap().clone();
        iter = nodes.keySet().iterator();
        if (old_size != nodes.size())
            populate();
    }
    
    @Override
    public boolean onSnapToItem(int x, int y, Point snapPoint, MapView mapView) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected OverlayItem createItem(int i) {
        Log.d(this.getClass().getSimpleName(), "drawing one node");
        if (!iter.hasNext())
            iter = nodes.keySet().iterator();
        
        OsmNode node = nodes.get(iter.next());
        GeoPoint geop = new GeoPoint(node.getLatitudeE6(), node.getLongitudeE6());
        OverlayItem oi = new OverlayItem(node.getName(), node.getOpening_hours(), geop);
        switch (node.isOpenNow()) {
        case CLOSED:
            oi.setMarker(act.getResources().getDrawable(R.drawable.closed));
            break;
        case OPEN:
            oi.setMarker(act.getResources().getDrawable(R.drawable.open));
            break;
        case MAYBE:
            oi.setMarker(act.getResources().getDrawable(R.drawable.maybe));
            break;
        case UNSET:
            oi.setMarker(act.getResources().getDrawable(R.drawable.dontknow));
            break;
        }
        return oi;
    }

    @Override
    public int size() {
        Log.d(this.getClass().getSimpleName(), "returning size of " + nodes.size() + " nodes");
        return nodes.size();
    }
}
