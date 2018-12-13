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

package org.cam.storage.levelgraph.storage.ondiskstorage;

import org.cam.storage.levelgraph.datatypes.Direction;
import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.storage.EdgeBloomFilter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

/*
Format is as follows:
    64 bytes for the number of elements in the block.
    64 bytes for the size of the levelNode index
    64 bytes for the edge list size
Index of edges and their offsets.
    id, edge count, offset, next block (id and index), previous block (id and index).

 */

public class StorageReaderWriter {

    private final int nodeBlockSize = 32;

    private final int edgeBlockSize = 18;

    private final int headerSize = 24;

    private int blockId;

    private MappedByteBuffer memoryMappedBuffer;

    private EdgeBloomFilter filter;

    private HashMap<Long, NodeData> nodeMapping;

    public StorageReaderWriter(String filename) {
        try {
            FileChannel fileChannel = new RandomAccessFile(new File(filename), "rw").getChannel(); //Write is for delete operations only. Delete does not remove element from the bloom filter though. Deletion support needs counting bloom filter. Would require implementing one on my own. Too much work. YOLO
            memoryMappedBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileChannel.size());
            int relativeIndex = 0;
            nodeMapping = new HashMap<>();
            int edgeCount = 0;
            //Edges are not loaded. Only the index and the bloom filter is loaded.
            if (fileChannel.size() > 0) {

                long nodeCount = memoryMappedBuffer.getLong();
                memoryMappedBuffer.getLong();
                memoryMappedBuffer.getLong();
                relativeIndex = headerSize;

                for (int counter = 0; counter < nodeCount; counter++) {
                    NodeData nodeData = new NodeData(memoryMappedBuffer, relativeIndex);
                    edgeCount += nodeData.edgeCount;
                    nodeMapping.put(nodeData.id, nodeData);
                    int nodeDataSize = nodeBlockSize;
                    relativeIndex += nodeDataSize;
                }
            }
            filter = new EdgeBloomFilter(new FileInputStream(filename + ".bloom"), edgeCount);
        }catch (Exception e){
            System.err.print(e.toString());
        }
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public Long getNodeCount() {
        return (long) nodeMapping.size();
    }

    /*
        Edge data: Neighbour, edgeid, direction
    */
    public long getNeighbour(long id, int edgeIndex) {
        if(nodeMapping.get(id)!=null){
            NodeData nodeData=nodeMapping.get(id);
            return memoryMappedBuffer.getLong(nodeData.offsets+edgeIndex*18);
        }else{
            return -1;
        }
    }

    public Direction getEdgeDirection(long id, int edgeIndex) {
         if(nodeMapping.get(id)!=null){
            NodeData nodeData=nodeMapping.get(id);
            return new Direction(memoryMappedBuffer.getChar(nodeData.offsets+edgeIndex*edgeBlockSize+16));
        }
        return new Direction('2');
    }

    public long getEdgeId(long id, int edgeIndex) {
          if(nodeMapping.get(id)!=null){
            NodeData nodeData=nodeMapping.get(id);
            return memoryMappedBuffer.getLong(nodeData.offsets+edgeIndex*edgeBlockSize+8);
        }
        return -1;
    }

    private Long getNeighbour(NodeData nodeData, int edgeIndex) {
        return memoryMappedBuffer.getLong(nodeData.offsets + edgeIndex * edgeBlockSize);
    }

    private int getNeighbourIndex(NodeData nodeData, int edgeIndex) {
        return nodeData.offsets + edgeIndex * edgeBlockSize;
    }

    private int getEdgeIdIndex(NodeData nodeData, int edgeIndex) {
        return nodeData.offsets + edgeIndex * edgeBlockSize + 8;
    }

    private int getDirectionIndex(NodeData nodeData, int edgeIndex) {
        return nodeData.offsets + edgeIndex * edgeBlockSize + 16;
    }

    private Edge unguardedGetEdge(long id, int edgeIndex){
             NodeData nodeData=nodeMapping.get(id);
            return new Edge(memoryMappedBuffer.getLong(nodeData.offsets+edgeIndex*edgeBlockSize),
                    memoryMappedBuffer.getLong(nodeData.offsets+edgeIndex*edgeBlockSize+8),
                    new Direction(memoryMappedBuffer.getChar(nodeData.offsets+edgeIndex*edgeBlockSize+16)),
                    null);
    }

    public Edge getEdge(long id, int edgeIndex){
           if(nodeMapping.get(id)!=null){
               return unguardedGetEdge(id,
                       edgeIndex);
        }
        return null;
    }

    public ArrayList<Edge> getEdges(long id) {
        ArrayList<Edge> results=new ArrayList<Edge>();
        if(nodeMapping.get(id)!=null){
            for (int i = 0; i < nodeMapping.get(id).edgeCount; i++) {
                results.add(unguardedGetEdge(id,i));
            }
        }
        return results;
    }

    int getNextBlock(long id){
        return nodeMapping.get(id).nextBlock;
    }

    void setNextBlock(long id, int nextBlock){
        memoryMappedBuffer.putInt(nextBlock,(int)nodeMapping.get(id).index+16);
        memoryMappedBuffer.force();
        nodeMapping.get(id).nextBlock=nextBlock;
    }

    void setPreviousBlock(long id, int previousBlock){
        memoryMappedBuffer.putInt(previousBlock,(int)nodeMapping.get(id).index+20);
        memoryMappedBuffer.force();
        nodeMapping.get(id).prevBlock=previousBlock;
    }

    int findEdge(Edge edge) {
        if (filter.mightContain(edge)) {
            if (nodeMapping.containsKey(edge.getSourceNode()) && nodeMapping.containsKey(edge.getNeighbour())) {
                NodeData data = nodeMapping.get(edge.getSourceNode());
                for (int i = 0; i < data.edgeCount; i++) {
                    if (memoryMappedBuffer.getLong(data.offsets + i * edgeBlockSize) == edge.getNeighbour()) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    void getEdge(Edge edge) {
        int index = findEdge(edge);
        if (index != -1) {
            NodeData nodeData = nodeMapping.get(edge.getSourceNode());
            edge.setEdgeid(memoryMappedBuffer.getLong(getEdgeIdIndex(nodeData, index)));
            edge.setDirection(memoryMappedBuffer.getChar(getDirectionIndex(nodeData, index)));
        }
    }

    int findEdge(Long from, Long to) {
        if (filter.mightContain(new Edge(from, to, (long) 0, '2', null)))
            if (nodeMapping.containsKey(from) && nodeMapping.containsKey(to)) {
                NodeData data = nodeMapping.get(from);
                for (int i = 0; i < data.edgeCount; i++) {
                    if (memoryMappedBuffer.getLong(data.offsets + i * edgeBlockSize) == to) {
                        return i;
                    }
                }
            }
        return -1;
    }

    private void swapEdge(NodeData data, int first, int second) {
        memoryMappedBuffer.getLong(getNeighbourIndex(data, first));
        byte[] firstByte = new byte[edgeBlockSize];
        memoryMappedBuffer.get(firstByte, getNeighbourIndex(data, first), edgeBlockSize);
        byte[] secondByte = new byte[edgeBlockSize];
        memoryMappedBuffer.get(secondByte, getNeighbourIndex(data, second), edgeBlockSize);
        memoryMappedBuffer.put(firstByte, getNeighbourIndex(data, second), edgeBlockSize);
        memoryMappedBuffer.put(secondByte, getNeighbourIndex(data, first), edgeBlockSize);
    }

    private void deleteEdgeInternal(@NotNull NodeData data, int index) {
        if (data.edgeCount != 1)
            swapEdge(data, index, data.edgeCount - 1);
        memoryMappedBuffer.putInt(data.edgeCount - 1, (int) (headerSize + data.index + 8));
    }

    public void deleteEdge(Edge edge) {
        int index = findEdge(edge.getSourceNode(), edge.getNeighbour());
        NodeData data = nodeMapping.get(edge.getSourceNode());
        deleteEdgeInternal(data, index);
        index = findEdge(edge.getNeighbour(), edge.getSourceNode());
        data = nodeMapping.get(edge.getNeighbour());
        deleteEdgeInternal(data, index);
    }
/*
    Advantage of LevelNode Data is that it gives a lockable object. Disadvantage: takes a lot of space.
    First version would have levelNode data as a lockable entity, later versions would throw that away.
 */
    private class NodeData{
        long id,index;
        int edgeCount, offsets, nextBlock, nextBlockIndex, prevBlock, prevBlockIndex;

    NodeData(@NotNull MappedByteBuffer buffer, int relativeIndex) {
            index=relativeIndex;
            id=buffer.getLong();
            edgeCount=buffer.getInt();
            offsets=buffer.getInt();
            nextBlock=buffer.getInt();
            nextBlockIndex=buffer.getInt();
            prevBlock=buffer.getInt();
            prevBlockIndex=buffer.getInt();
        }
    }






    //Need to write a get property call as well. Might be, I can just store that in a key-value store somewhere. Each edge having a unique id.
}
