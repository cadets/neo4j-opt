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


import java.io.File;
import java.io.IOException;

/*

This relies on the functionality provided by Tape.
It does not have a journal,
so it is pretty pointless if failure occurs between a remove or a write operation.

*/


public class PersistentQueue implements QueueInterface {

    private QueueFile queueFile;

    public PersistentQueue(File file) throws IOException {
        queueFile = new QueueFile.Builder(file).build();
    }

    @Override
    public void addData(byte[] data) {
        try {
            queueFile.add(data);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public byte[] getData() {
        byte[] data = null;
        try {
            data = queueFile.peek();
            queueFile.remove();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return data;
    }

    @Override
    public byte[] peek() {
        try {
            return queueFile.peek();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public byte[] readHead() throws IOException {
        return queueFile.readHead();
    }

    @Override
    public boolean isEmpty() {
        return queueFile.isEmpty();
    }

    @Override
    public void clear() {
        try {
            queueFile.clear();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


    @Override
    public long getSize() {
        return queueFile.usedBytes();
    }

    @Override
    public int getElementCount() {
        return queueFile.size();
    }

}
