package org.yaoha;

import java.util.HashMap;

public interface NodesQueryInterface<Key, Value> {
    HashMap<Key, Value> getAllNodes();
    HashMap<Key, Value> getNodesFromMapExtract(int left, int top, int right, int bottom);
    void addListener(NodeReceiverInterface<OsmNode> irec);
}
