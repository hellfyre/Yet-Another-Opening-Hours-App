/*
 *  This file is part of YAOHA.
 *
 *  YAOHA is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YAOHA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YAOHA.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright 2012 Stefan Hobohm, Lutz Reinhardt, Matthias Uschok
 *
 */

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
