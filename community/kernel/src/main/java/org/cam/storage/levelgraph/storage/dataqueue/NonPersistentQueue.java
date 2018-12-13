/*
 *   Copyright (c) 2018.
 *   This file is part of NeGraph.
 *
 *  NeGraph is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NeGraph is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NeGraph.  If not, see <https://www.gnu.org/licenses/>.
 * @author Jyothish Soman, cl cam uk
 */

package org.cam.storage.levelgraph.storage.dataqueue;

import java.util.LinkedList;
import java.util.Queue;

public class NonPersistentQueue implements QueueInterface {

    private Queue<ObjectWrapper> queue;

    public NonPersistentQueue() {
        this.queue = new LinkedList<ObjectWrapper>();
    }

    @Override
    public void addData(byte[] data) {
        queue.add(new ObjectWrapper(data));
    }

    @Override
    public byte[] getData() {
        ObjectWrapper data = queue.peek();
        queue.remove();
        return data.bytes;
    }

    @Override
    public byte[] peek() {
        return queue.peek().bytes;
    }


    @Override
    public int getElementCount() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void clear() {
        queue.clear();
    }

    private class ObjectWrapper {
        byte[] bytes;

        public ObjectWrapper(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    @Override
    public long getSize() {
        return queue.size();
    }
}
