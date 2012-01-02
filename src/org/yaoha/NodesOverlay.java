package org.yaoha;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

public class NodesOverlay extends ItemizedOverlay<OverlayItem> {
    HashMap<Integer, OsmNode> nodes;
    Iterator<Integer> iter;
    Activity act;
    long last_toast_started;
    OsmNode last_node;
    
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
    
    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        boolean ret_val = super.onTouchEvent(event, mapView);
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return ret_val;
        
        if (System.currentTimeMillis() < last_toast_started + 5000) {
            // start activity displaying more information, which can be editable
            Intent intent = new Intent("displayStuff");
            intent = new Intent(act, NodeActivity.class);
            
            Set<String> keys = last_node.getKeys();
            for (String key : keys) {
                intent.putExtra(key, last_node.getAttribute(key));
            }
            act.startActivity(intent);
        }
        
        float x = event.getX();
        float y = event.getY();
        Log.d(this.getClass().getSimpleName(), "touch event at (" + x + ", " + y + ")");
        
        Drawable image_size = act.getResources().getDrawable(R.drawable.closed);
        int offset_x = image_size.getMinimumWidth();
        int offset_y = image_size.getMinimumHeight();
        
        IGeoPoint event_on_map_minus_offset = mapView.getProjection().fromPixels(x-offset_x, y-0*offset_y);
        IGeoPoint event_on_map_plus_offset = mapView.getProjection().fromPixels(x+offset_x, y+2*offset_y);
        
        Log.d(this.getClass().getSimpleName(), "event on map is from " + event_on_map_minus_offset + " to " + event_on_map_plus_offset);
        Rect rect_around_event = new Rect(event_on_map_minus_offset.getLongitudeE6(),
                event_on_map_plus_offset.getLatitudeE6(),
                event_on_map_plus_offset.getLongitudeE6(),
                event_on_map_minus_offset.getLatitudeE6());
         
        OsmNode n = null;
        @SuppressWarnings("unchecked")
        HashMap<Integer, OsmNode> nodes = (HashMap<Integer, OsmNode>) Nodes.getInstance().getNodeMap().clone();
        for (Integer index : nodes.keySet()) {
            OsmNode tmp_node = nodes.get(index);
            if (rect_around_event.contains(tmp_node.getLongitudeE6(), tmp_node.getLatitudeE6())) {
                n = tmp_node;
                break;
            }
        }
        
        if (n != null) {
            String text = "name = " + n.getName()
                    + "\n opening_hours = " + n.getOpening_hours();
            Toast t = Toast.makeText(mapView.getContext(), text, Toast.LENGTH_SHORT);
            last_toast_started = System.currentTimeMillis();
            last_node = n;
            t.show();
        }
        
        return ret_val;
    }
}
