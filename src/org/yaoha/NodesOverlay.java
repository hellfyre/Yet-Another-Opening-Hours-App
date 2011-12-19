package org.yaoha;

import java.util.HashMap;

import microsoft.mappoint.TileSystem;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.PathOverlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class NodesOverlay extends Overlay {
    
    public NodesOverlay(Context ctx) {
        super(ctx);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        if (shadow)
            return;

        final int offset = 20;
        
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
            switch (node.isOpenNow()) {
                case CLOSED:
                    paint.setColor(Color.RED);
                    break;
                case OPEN:
                    paint.setColor(Color.GREEN);
                    break;
                case MAYBE:
                    paint.setColor(Color.BLUE);
                    break;
                default:
                    paint.setColor(Color.BLACK);
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
            
            c.drawCircle(pt.x, pt.y, 10, paint);
        }
    }
}
