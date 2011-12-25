package org.yaoha;

import java.util.HashMap;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

public class NodesOverlay extends Overlay {
    final int offset = 20;
    
    public NodesOverlay(Context ctx) {
        super(ctx);
    }
    
    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        if (shadow)
            return;
        
        //Define brush 1
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
//        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        
        paint.setTextAlign(Paint.Align.LEFT);
        
        // Calculate the half-world size
        final Rect viewportRect = new Rect();
        final Projection projection = osmv.getProjection();
        // Save the Mercator coordinates of what is on the screen
        viewportRect.set(projection.getScreenRect());
        
        @SuppressWarnings("unchecked")
        HashMap<Integer, OsmNode> nodes = (HashMap<Integer, OsmNode>) Nodes.getInstance().getNodeMap().clone();
        for (Integer index : nodes.keySet()) {
            OsmNode node = nodes.get(index);
            int res_id;
            switch (node.isOpenNow()) {
                case CLOSED:
                    res_id = R.drawable.closed;
                    break;
                case OPEN:
                    res_id = R.drawable.open;
                    break;
                case MAYBE:
                    res_id = R.drawable.maybe;
                    break;
                default:
                    res_id = R.drawable.dontknow;
                    break;
            }
            //Translate point to x y coordinates on the screen
            IGeoPoint igeo_in = new GeoPoint(node.getLatitudeE6(), node.getLongitudeE6());
            Point pt = projection.toMapPixels(igeo_in, null);
            
            //Is the node outside the viewing area? If yes, do not draw it
            if (!viewportRect.contains(pt.x-offset, pt.y-offset) 
                    && !viewportRect.contains(pt.x-offset, pt.y+offset) 
                    && !viewportRect.contains(pt.x+offset, pt.y+offset) 
                    && !viewportRect.contains(pt.x+offset, pt.y-offset) )
                continue;
            
            Log.d(NodesOverlay.class.getSimpleName(), "drawing one node");
            
            Resources res = osmv.getContext().getResources();
            Bitmap bm = BitmapFactory.decodeResource(res, res_id);
            
            c.drawBitmap(bm, pt.x - bm.getWidth()/2, pt.y - bm.getHeight()/2, paint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        boolean ret_val = super.onTouchEvent(event, mapView);
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return ret_val;
        
        float x = event.getX();
        float y = event.getY();
        Log.d(this.getClass().getSimpleName(), "touch event at (" + x + ", " + y + ")");
        
        IGeoPoint event_on_map_minus_offset = mapView.getProjection().fromPixels(x-offset, y-offset);
        IGeoPoint event_on_map_plus_offset = mapView.getProjection().fromPixels(x+offset, y+offset);
        
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
            // test if n was in the area of the touchevent
            if (rect_around_event.contains(tmp_node.getLongitudeE6(), tmp_node.getLatitudeE6())) {
                n = tmp_node;
                break;
            }
        }
        
        if (n != null) {
            String text = "name = " + n.getName() 
                    + "\n opening_hours = " + n.getOpening_hours();
            Toast.makeText(mapView.getContext(), text, Toast.LENGTH_SHORT).show();
        }
        
        return ret_val;
    }
}
