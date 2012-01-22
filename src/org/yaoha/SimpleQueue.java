package org.yaoha;

import java.util.LinkedList;

public class SimpleQueue<E> {
    private LinkedList<E> list = new LinkedList<E>();

    public void add(E object) {
        list.addLast(object);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public E remove() {
        if (isEmpty()) {
            return null;
        }
        return list.removeFirst();
    }

    public int size() {
        return list.size();
    }
 

}
