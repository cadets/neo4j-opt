

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

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.cam.storage.levelgraph.Pair;
import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.datatypes.PropertyEntity;
import org.cam.storage.levelgraph.storage.FileWriter;
import org.cam.storage.levelgraph.storage.RocksDBInterface;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Provides an abstraction for all file based operations.
 */

public class FileStorageLayerInterface {

    private RocksDBInterface rocksDBInterface;

    // 0: LevelNode external to internal
    // 1: LevelNode, Edge type to Sub-levelNode. Not really using this right now. Rest of all are persisted.
    // 2: LevelNode to start buffer
    // 3: LevelNode to last buffer
    // 4: Last used bufferCount

    private HashMap<Long, StorageReaderWriter> nodeToBufferMap;

    private HashMap<Long, StorageReaderWriter> nodeToLastBufferMap;

    private HashMap<Long, StorageReaderWriter> IdMapper;

    private Integer bufferCount; //This is a relative number. Need to handle this properly, else will be super buggy.

    private String prefix;

    public FileStorageLayerInterface(String path) {
        File directory = new File(path);
        if (! directory.exists()){
            directory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        prefix = path + "/";
        bufferCount = -1;
        nodeToBufferMap = new HashMap<>();
        nodeToLastBufferMap = new HashMap<>();
        IdMapper = new HashMap<>();
    }

    private String getFileName(int number) {
        return prefix + "level" + number + ".dat";
    }

    private String getNextFileName(int level) {
        bufferCount++;
        return getFileName(bufferCount);
    }

    public void setRocksDBInterface(RocksDBInterface rocksDBInterface) {
        this.rocksDBInterface = rocksDBInterface;
        if(rocksDBInterface.keyMayExist((long)0,4)) {
            bufferCount = rocksDBInterface.getValue((long) 0, 4).intValue();
        }
        if(bufferCount==-1){
            bufferCount = 0;
        }
        for (int i = 0; i < bufferCount; i++) {
            IdMapper.put((long) i, new StorageReaderWriter(getFileName(i)));
            IdMapper.get((long) i).setBlockId(i);
        }
    }

    public String writeToFile(HashMap<Long, ArrayList<Edge>> edgeList, HashMap<Long, Pair<Integer, Integer>> prevData) {
        String fileName = getNextFileName(0);
        FileWriter fileWriter = new FileWriter(fileName);
        fileWriter.writeToFile(edgeList, prevData);
        StorageReaderWriter storageReaderWriter;
        rocksDBInterface.setValue((long) 0, (long) bufferCount, 4);// 4: Last used buffer
        storageReaderWriter = new StorageReaderWriter(fileName);
        storageReaderWriter.setBlockId(bufferCount);
        IdMapper.put((long) bufferCount, storageReaderWriter);
        for (long levelNode : edgeList.keySet()) {
            if (!nodeToBufferMap.containsKey(levelNode)) {
                nodeToBufferMap.put(levelNode, storageReaderWriter);
                if (rocksDBInterface.getValue(levelNode, 2) == -1) {
                    rocksDBInterface.setValue(levelNode, (long) bufferCount, 2); //2: LevelNode to start buffer
                    rocksDBInterface.setValue(levelNode, levelNode, 0);
                }
            }
            if (nodeToLastBufferMap.get(levelNode) != null) {
                nodeToLastBufferMap.get(levelNode).setNextBlock(levelNode, storageReaderWriter.getBlockId());
                storageReaderWriter.setPreviousBlock(levelNode, nodeToLastBufferMap.get(levelNode).getBlockId());
            }
            nodeToLastBufferMap.put(levelNode, storageReaderWriter);
            rocksDBInterface.setValue(levelNode, (long) bufferCount, 3); //3: LevelNode to last buffer
        }
        return fileName;
    }

    public LevelNode getNode(LevelNode levelNode) {
        Long internalId = rocksDBInterface.getValue(levelNode.getInternalId(), 0);
        LevelNode result = null;
        if (internalId != -1) {
            result = new LevelNode(internalId);
        }
        return result;
    }

    public RelationshipRecord returnRecord(long neo4jId, RelationshipRecord record) throws RocksDBException{
        String value= rocksDBInterface.getStringValue(neo4jId, 4);
        if(value==null){
            throw new RocksDBException("Key not found");
        }
        String[] splits=value.split("-");
        long node= Longs.fromByteArray(splits[0].getBytes());
        long secondNode=Longs.fromByteArray(splits[1].getBytes());
        int type= Ints.fromByteArray(splits[2].getBytes());
        long fpr=Longs.fromByteArray(splits[3].getBytes());
        long fnr=Longs.fromByteArray(splits[4].getBytes());
        long spr=Longs.fromByteArray(splits[5].getBytes());
        long snr=Longs.fromByteArray(splits[6].getBytes());
        long nextProp=Longs.fromByteArray(splits[7].getBytes());
        boolean fifc= splits[8].charAt(0)=='1';
        boolean fisc= splits[8].charAt(1)=='1';
        boolean inUse=splits[8].charAt(2)=='1';
        record.setId(neo4jId);
        record.initialize(inUse, nextProp,
                node, secondNode, type, fpr, fnr, spr, snr,
                fifc, fisc);
        return record;
    }


    public RelationshipRecord returnRecord(long neo4jId){
        String value= rocksDBInterface.getStringValue(neo4jId, 4);
        if(value==null){
            return null;
        }
        String[] splits=value.split("-");
        long node= Longs.fromByteArray(splits[0].getBytes());
        long secondNode=Longs.fromByteArray(splits[1].getBytes());
        int type= Ints.fromByteArray(splits[2].getBytes());
        long fpr=Longs.fromByteArray(splits[3].getBytes());
        long fnr=Longs.fromByteArray(splits[4].getBytes());
        long spr=Longs.fromByteArray(splits[5].getBytes());
        long snr=Longs.fromByteArray(splits[6].getBytes());
        long nextProp=Longs.fromByteArray(splits[7].getBytes());
        boolean fifc= splits[8].charAt(0)=='1';
        boolean fisc= splits[8].charAt(1)=='1';
        boolean inUse=splits[8].charAt(2)=='1';
        RelationshipRecord record=new RelationshipRecord(neo4jId).
                initialize(inUse, nextProp,
                node, secondNode, type, fpr, fnr, spr, snr,
                fifc, fisc);
        return record;
    }

    public void addRecord(RelationshipRecord record, long neo4jId){
        String value=record.getFirstNode()+"-"+record.getSecondNode()+"-"+record.getType()+"-"+
                record.getFirstPrevRel()+"-"+record.getFirstNextRel()+"-"+record.getSecondPrevRel()+"-"
                +record.getNextProp()+"-"+ (record.isFirstInFirstChain()?"1":"0")+(record.isFirstInSecondChain()?"1":"0")
                +(record.inUse()?"1":"0");
        rocksDBInterface.setValue(neo4jId, value, 4);
    }



    public boolean hasNode(long nodeId) {
        return rocksDBInterface.getValue(nodeId, 2) != -1;
    }

    public ArrayList<Edge> getEdges(LevelNode levelNode, long queryingTransaction) {
        ArrayList<Edge> results = new ArrayList<Edge>();
        StorageReaderWriter reader = nodeToBufferMap.get(levelNode.getInternalId());
        while (reader != nodeToLastBufferMap.get(levelNode.getInternalId())) {
            results.addAll(reader.getEdges(levelNode.getInternalId()));
            reader = IdMapper.get((long) reader.getNextBlock(levelNode.getInternalId()));
        }
        return results;
    }

    public ArrayList<PropertyEntity> getProperties(LevelNode levelNode) {
        //TODO
        return null;
    }

    public ArrayList<PropertyEntity> getProperties(Edge edge) {
        //TODO
        return null;
    }

    public void deleteEdge(Edge edge) {
        for (StorageReaderWriter storagereader : IdMapper.values()
        ) {
            int edgeindex = storagereader.findEdge(edge);
            if (edgeindex != -1) {
                storagereader.deleteEdge(edge);
            }
        }
    }

    /*
     */
    public void mergeFiles() {

    }
    /* Need to implement file merging stuff. */
}
