package org.yaoha;

import java.util.HashMap;

public class Nodes {
    private HashMap<Integer, OsmNode> nodeMap;

    // Nodes class can not be instantiated by other classes
    private Nodes() {
        nodeMap = new HashMap<Integer, OsmNode>();
    }
    
    public HashMap<Integer, OsmNode> getNodeMap() {
        return nodeMap;
    }
    
    private static class SingletonHolder {
        public static final Nodes instance = new Nodes();
    }
    
    public static Nodes getInstance() {
        return SingletonHolder.instance;
    }

}
