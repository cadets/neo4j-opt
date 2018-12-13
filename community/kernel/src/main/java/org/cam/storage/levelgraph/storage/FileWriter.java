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

package org.cam.storage.levelgraph.storage;

import org.cam.storage.levelgraph.Pair;
import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
/*
Format is as follows:
    64 bytes for the number of elements in the block.
    64 bytes for the size of the levelNode index
    64 bytes for the edge list size
Index of edges and their offsets.
    id, edge count, offset, next block (id and index), previous block (id and index).

 */

public class FileWriter {


    private static int nodeBlockSize = 32;
    private static int edgeBlockSize = 18;
    private static int headerSize = 24;
    private MappedByteBuffer mbuffer;
    private String fileName;
    private String bloomFilterName;
    private FileChannel fileChannel;

    public FileWriter(String filename, HashMap<Long, ArrayList<Edge>> edgeList, HashMap<Long, Pair<Integer, Integer>> prevData) {
        fileName = filename;
        openFile(edgeList);
        writeToFile(edgeList, prevData);
    }

    public FileWriter(String fileName) {
        this.fileName = fileName;
        mbuffer = null;
    }

    private void openFile(HashMap<Long, ArrayList<Edge>> edgeList) {
        try {

            fileChannel = new RandomAccessFile(new File(fileName), "rw").getChannel();
            long fileSize = calculateFileSize(edgeList);
            mbuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
        } catch (Exception e) {
            System.err.println("File writer facing errors and failed");
        }
    }

    public MappedByteBuffer getMbuffer() {
        return mbuffer;
    }

    //        public long id;
    //        public int edgeCount, offsets, nextBlock, nextBlockIndex, prevBlock, prevBlockIndex;

    private long calculateFileSize(HashMap<Long, ArrayList<Edge>> edgeList) {

        long fileSize = 0;

        fileSize += 24; //Header length
        if (edgeList != null) {

            fileSize += edgeList.size() * nodeBlockSize; //Size of levelNode index
            for (ArrayList<Edge> element : edgeList.values()) {
                element.size();
                fileSize += element.size() * edgeBlockSize;
            }
        }
        fileSize += 32;

        return fileSize;
    }


    public void writeToFile(HashMap<Long, ArrayList<Edge>> edgeList, HashMap<Long, Pair<Integer, Integer>> prevData) {
        if (mbuffer == null) {
            openFile(edgeList);
        }
        bloomFilterName = fileName + ".bloom";
        int nodes = 0;
        if (edgeList != null) nodes = edgeList.size();
        mbuffer.putLong(nodes);
        mbuffer.putLong(headerSize); //Start of the levelNode index: Its always 24.
        mbuffer.putLong(headerSize + nodes * nodeBlockSize); //Start of the edgelist: It is levelNode count*32+headersize

        try {
            OutputStream output = new FileOutputStream(bloomFilterName);

            HashSet<Long> levelNodeSet;
            levelNodeSet= edgeList!=null?new HashSet<>(edgeList.keySet()):new HashSet<>();
            int currentOffset = headerSize + nodes * nodeBlockSize;
            int totalEdgeCount = 0;
            for (Long levelNode : levelNodeSet) {
                if (levelNode == null) {
                    System.out.println("Null levelNode found here");
                    continue;
                }
                mbuffer.putLong(levelNode);
                int nodeEdgeCount = edgeList.get(levelNode).size();
                mbuffer.putInt(edgeList.get(levelNode).size());
                mbuffer.putInt(currentOffset);
                if (prevData != null && prevData.get(levelNode) != null) {
                    mbuffer.putInt(prevData.get(levelNode).getFirst());
                    mbuffer.putInt(prevData.get(levelNode).getSecond());
                } else {
                    mbuffer.putInt(-1);
                    mbuffer.putInt(-1);
                }
                mbuffer.putInt(-1);
                mbuffer.putInt(-1);
                currentOffset += edgeBlockSize * nodeEdgeCount;
                totalEdgeCount += nodeEdgeCount;
            }
            EdgeBloomFilter filter = new EdgeBloomFilter(totalEdgeCount);
            for (Long levelNode : levelNodeSet) {
                ArrayList<Edge> nodeEdges = edgeList.get(levelNode);
                for (Edge iter :nodeEdges) {
                    filter.addElement(iter);
                    mbuffer.putLong(iter.getNeighbour());
                    mbuffer.putLong(iter.getEdgeid());
                    mbuffer.putChar(iter.getDirection().getDirection());
                }
            }
            filter.writeTo(output);
        } catch (NullPointerException e) {
            System.err.println("Null point exception caught");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        mbuffer.force();
        try {
            fileChannel.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

}
