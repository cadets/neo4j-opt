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

import org.cam.storage.levelgraph.UpdatesCache;
import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.storage.DataStorageInterface;
import org.cam.storage.levelgraph.storage.RocksDBInterface;
import org.neo4j.values.storable.Value;

import java.util.ArrayList;

/**
 * This is the primary orchestrating class in the storage. All the data in the storage is accessible here.
 * All accesses to the validated and stored data will be here.
 * For distributed storage, this class will be the interface through which the updates can be read.
 */

public class UnifiedDataStorage implements DataStorageInterface {

    /**
     * Meta data stored here.
     */

    /**
     * The cache which holds updates which needs to be flushed to disk every time the cache fills up.
     */
    UpdatesCache updatesCache;

    FileStorageLayerInterface fileStorageLayerInterface;

    // 0: LevelNode external to internal
    // 1: LevelNode, Edge type to Sub-levelNode
    // 2: LevelNode to start buffer
    // 3: LevelNode to last buffer
    // 4: Last used bufferCount

    // TODO merge this once the transaction system is working.

    /**
     * Initialise the file based persistence of the various components.
     *
     * @param path The absolute path of the data storage directory.
     */

    public UnifiedDataStorage(String path) {
        if (path == null || path.length() == 0) {
            path = "fileStore";
        }

        fileStorageLayerInterface = new FileStorageLayerInterface(path + "/dataStore");
        fileStorageLayerInterface.setRocksDBInterface(new RocksDBInterface(path + "/db", 5));

        updatesCache = new UpdatesCache(fileStorageLayerInterface);
    }


    /**
     * Data always gets written to the UpdatesCache as it is responsible for creating filechunks whenever needed.
     * This function pushes the node into the updatesCache where the node now has an internal id which is unique.
     *
     * @param levelNode
     * @return Id of the new node.
     */
    @Override
    public Long addNode(LevelNode levelNode) {
        return updatesCache.addNode(levelNode);
    }

    /**
     * Add an edge starting from a node into the storage.
     * TODO: There is something not right about this function.
     *
     * @param from
     * @param edge
     * @return
     */
    @Override
    public Long addEdge(Long from, Edge edge) {
        return updatesCache.addEdge(from, edge);
    }

    public void addEdge(Edge edge) {
        updatesCache.addEdge(edge.getSourceNode(), edge);
    }

    @Override
    public ArrayList<Edge> getEdges(LevelNode levelNode, long queryingTransaction) {
        ArrayList<Edge> results = updatesCache.getEdges(levelNode, queryingTransaction);
        results.addAll(fileStorageLayerInterface.getEdges(levelNode, queryingTransaction));
        return results;
    }

    public LevelNode getNode(LevelNode levelNode) {
        LevelNode result = updatesCache.getNode(levelNode);
        if (result != null) {
            return result;
        }
        result = fileStorageLayerInterface.getNode(levelNode);
        return result;
    }

    public boolean hasNode(long node) {
        if (updatesCache.hasNode(node)) {
            return true;
        }
        return fileStorageLayerInterface.hasNode(node);
    }

    public boolean hasEdge(long edgeId) {
        //TODO
        return false;

    }
    public void addProperty(LevelNode levelNode, String property) {

    }

    public void deleteEdge(Edge edge, long deletingTransaction) {
        if (!updatesCache.deleteEdge(edge.getNeighbour(), edge.getNeighbour(), deletingTransaction)) {
            fileStorageLayerInterface.deleteEdge(edge);
        } else {
            fileStorageLayerInterface.deleteEdge(edge);
        }
    }

    public void deleteNode(LevelNode levelNode, long deletingTransaction) {
    }

    @Override
    public ArrayList<Value> getProperties(LevelNode levelNode) {
        return null;
    }

    @Override
    public ArrayList<Value> getProperties(Edge edge) {
        return null;
    }

    @Override
    public void addProperty(LevelNode node, Value properties, int id) {

    }

    @Override
    public void addProperty(Edge edge, Value properties, int id) {

    }

    @Override
    public Value getNodeProperty(int propertyId) {
        return null;
    }

    @Override
    public Value getRelationshipProperty(int propertyId) {
        return null;
    }
}
