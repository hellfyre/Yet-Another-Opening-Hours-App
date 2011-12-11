package org.yaoha;

import java.util.HashMap;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
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
        
        //Define brush 1
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        
        HashMap<Integer, OsmNode> nodes = Nodes.getInstance().getNodeMap();
        for (Integer index : nodes.keySet()) {
            OsmNode node = nodes.get(index);
            // TODO draw node
            //Translate point to x y coordinates on the screen
            IGeoPoint igeo = new GeoPoint(node.getLatitudeE6(), node.getLongitudeE6());
            Point pt = osmv.getProjection().toPixels(igeo, null);
            
            //Is the node outside the viewing area? If yes, do not draw it
            if(pt.x<-90 || pt.x>osmv.getWidth()+50 || pt.y<-50 || pt.y>osmv.getHeight()+50)
                continue;
            
            Log.d(NodesOverlay.class.getSimpleName(), "drawing one node");
            //Define path
            final int offset = 20;
            Path path = new Path();
            path.moveTo(pt.x-offset, pt.y-offset);
            
            path.lineTo(pt.x+offset, pt.y-offset);
            path.lineTo(pt.x+offset, pt.y+offset);
            path.lineTo(pt.x-offset, pt.y+offset);
            path.lineTo(pt.x-offset, pt.y-offset);
            
            c.drawPath(path, paint);
        }
        
    }

}
