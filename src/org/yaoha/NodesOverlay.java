package org.yaoha;

import java.util.HashMap;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;

public class NodesOverlay extends Overlay {
    
    public NodesOverlay(Context ctx) {
        super(ctx);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        if (shadow)
            return;
        HashMap<Integer, OsmNode> nodes = Nodes.getInstance().getNodeMap();
        for (Integer index : nodes.keySet()) {
            OsmNode node = nodes.get(index);
            // TODO draw node
        }
        
    }

}
