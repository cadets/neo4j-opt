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


import com.google.common.primitives.Bytes;
import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.datatypes.PropertyEntity;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WriteAheadLog implements WALInterface {

    public static final Map<String, Short> idMap;
    private static final Map<Short, String> reverseMap;

    static {
        Map<Short, String> shortStringMap = new HashMap<>();
        idMap = new HashMap<>();
        idMap.put("NodeCreate", (short) 0);
        idMap.put("NodeDelete", (short) 1);
        idMap.put("NodePropertyCreate", (short) 2);
        idMap.put("NodePropertyDelete", (short) 3);
        idMap.put("NodePropertyUpdate", (short) 4);
        idMap.put("EdgeCreate", (short) 5);
        idMap.put("EdgeDelete", (short) 6);
        idMap.put("EdgePropertyCreate", (short) 7);
        idMap.put("EdgePropertyDelete", (short) 8);
        idMap.put("EdgePropertyUpdate", (short) 9);
        shortStringMap.put((short) 0, "NodeCreate");
        shortStringMap.put((short) 1, "NodeDelete");
        shortStringMap.put((short) 2, "NodePropertyCreate");
        shortStringMap.put((short) 3, "NodePropertyDelete");
        shortStringMap.put((short) 4, "NodePropertyUpdate");
        shortStringMap.put((short) 5, "EdgeCreate");
        shortStringMap.put((short) 6, "EdgeDelete");
        shortStringMap.put((short) 7, "EdgePropertyCreate");
        shortStringMap.put((short) 8, "EdgePropertyDelete");
        shortStringMap.put((short) 9, "EdgePropertyUpdate");
        reverseMap = Collections.unmodifiableMap(shortStringMap);
//        idMap = Collections.unmodifiableMap(bMap);
    }

    PersistentQueue wal;
    int maxSize;

    public WriteAheadLog(String filepath) {
        openQueue(filepath);
        maxSize = 4096;
    }

    public WriteAheadLog(String filepath, int maxSize) {
        openQueue(filepath);
        this.maxSize = maxSize;
    }

    private void openQueue(String filepath) {
        File directory = new File(filepath);
        if (! directory.exists()){
            directory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        File file = new File(filepath + "/lgph.wal");
        try {
            wal = new PersistentQueue(file);
        } catch (IOException e) {

        }
    }

    @Override
    public void addData(byte[] data) {
        wal.addData(data);
    }

    @Override
    public byte[] getData() {
        return wal.getData();
    }

    @Override
    public void clear() {
        wal.clear();
    }

    @Override
    public boolean isFull() {
        return wal.getSize() >= maxSize;
    }


    @Override
    public void addNode(LevelNode levelNode) {
        byte[] idBytes = idMap.get("NodeCreate").toString().getBytes();
        wal.addData(Bytes.concat(idBytes, levelNode.toBytes()));
    }

    @Override
    public void addEdge(Edge edge) {
        byte[] idBytes = idMap.get("EdgeCreate").toString().getBytes();
        wal.addData(Bytes.concat(idBytes, edge.toBytes()));
    }


    @Override
    public void addNodeProperty(LevelNode levelNode, PropertyEntity properties) {
        byte[] idBytes = idMap.get("NodePropertyCreate").toString().getBytes();
        byte[] nodeBytes = levelNode.getInternalId().toString().getBytes();
        byte[] propertyBytes = properties.toBytes();
        wal.addData(Bytes.concat(idBytes, nodeBytes, propertyBytes));
    }

    @Override
    public void addEdgeProperty(Edge edge, PropertyEntity properties) {
        byte[] idBytes = idMap.get("EdgePropertyCreate").toString().getBytes();
        wal.addData(Bytes.concat(idBytes, ((Long) edge.getEdgeid()).toString().getBytes(), properties.toBytes()));
    }

    @Override
    public void deleteNode(LevelNode levelNode) {
        byte[] idBytes = idMap.get("NodeDelete").toString().getBytes();
        wal.addData(Bytes.concat(idBytes, levelNode.getInternalId().toString().getBytes()));

    }

    @Override
    public void deleteEdge(Edge edge) {
        byte[] idBytes = idMap.get("EdgeDelete").toString().getBytes();
        wal.addData(Bytes.concat(idBytes, ((Long) edge.getEdgeid()).toString().getBytes()));
    }

    @Override
    public void deleteNodeProperty(LevelNode levelNode, PropertyEntity properties) {
        byte[] idBytes = idMap.get("NodePropertyDelete").toString().getBytes();
        wal.addData(Bytes.concat(idBytes, levelNode.getInternalId().toString().getBytes(), properties.toBytes()));
    }

    @Override
    public void deleteEdgeProperty(Edge edge, PropertyEntity properties) {
        byte[] idBytes = idMap.get("EdgePropertyDelete").toString().getBytes();
        wal.addData(Bytes.concat(idBytes, ((Long) edge.getEdgeid()).toString().getBytes(), properties.toBytes()));
    }

    @Override
    public boolean isEmpty() {
        return wal.isEmpty();
    }

    @Override
    public byte[] getNext() {
        return wal.getData();
    }


}
