package org.yaoha;

public interface NodeReceiverInterface<Value> {
    void put(Value value);
    void requeryBoundingBox();
}
