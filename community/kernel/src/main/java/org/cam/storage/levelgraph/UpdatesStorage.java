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

package org.cam.storage.levelgraph;

import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.storage.DataStorageInterface;
import org.neo4j.values.storable.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class UpdatesStorage implements DataStorageInterface {

    HashMap<Long, ArrayList<Edge>> cachedEdges;

    HashMap<Long, LevelNode> nodeMap;

    HashMap<Long, Pair<Integer, Integer>> prevData;

    HashMap<Long, HashSet<Integer>> nodeProperties;

    HashMap<Integer, Value> nodePropertyMap;

    HashMap<Long, HashSet<Integer>> relationshipProperties;

    HashMap<Integer, Value> relationshipPropertyMap;

    HashMap<Long, HashSet<Integer>> nodeLabels;

    HashMap<Integer, String> labelIdToString;

    Integer currentBlock;

    public UpdatesStorage() {
        cachedEdges = new HashMap<>();
        nodeMap = new HashMap<>();
        prevData = new HashMap<>();
        nodeProperties = new HashMap<>();
        nodePropertyMap = new HashMap<>();
        relationshipProperties = new HashMap<>();
        relationshipPropertyMap = new HashMap<>();
        nodeLabels = new HashMap<>();
        labelIdToString = new HashMap<>();
        currentBlock = 0;
    }

    public LevelNode getNode(Long node) {
        if (nodeMap.containsKey(node)) {
            return nodeMap.get(node);
        }
        return null;
    }

    public boolean hasNode(Long node) {
        return nodeMap.containsKey(node);
    }

    public LevelNode getNode(LevelNode levelNode) {
        return getNode(levelNode.getInternalId());
    }

    private boolean removeEdgeInternal(Long from, Long to, Long deletingTransaction) {
        if (nodeMap.containsKey(from) && cachedEdges.containsKey(from)) {
            int id = cachedEdges.get(from).indexOf(new Edge(from, to, (long) -1, '2', null)); //direction bug TODO
            cachedEdges.get(from).get(id).setDeletingTransaction(deletingTransaction);
            return true;
        }
        return false;
    }

    public Long addEdge(Long nodeId, Edge edge) {
        cachedEdges.putIfAbsent(nodeId, new ArrayList<Edge>()); //What if there is a duplicate edge? Use bloom filter?
        cachedEdges.get(nodeId).add(edge);
        return edge.getEdgeid();
    }

    @Override
    public ArrayList<Value> getProperties(LevelNode levelNode) {
        ArrayList<Value> results = new ArrayList<>();
        for (Integer e : nodeProperties.get(levelNode.getInternalId())
        ) {
            results.add(nodePropertyMap.get(e));
        }
        return results;
    }

    @Override
    public ArrayList<Value> getProperties(Edge edge) {
        ArrayList<Value> results = new ArrayList<>();
        for (Integer e : relationshipProperties.get(edge.getEdgeid())
        ) {
            results.add(relationshipPropertyMap.get(e));
        }
        return results;
    }


    // This operation removes any edge that has not yet been flushed to disk.
    // If deletion fails, it is upto the storage to handle it.

    public boolean deleteEdge(Long from, Long to, Long deletingTransaction) {
        //Delete paired edge
        return removeEdgeInternal(from, to, deletingTransaction) || removeEdgeInternal(to, from, deletingTransaction);
    }

    public Long addNode(Long nodeId) {
        cachedEdges.put(nodeId, new ArrayList<>());
        prevData.put(nodeId, new Pair<Integer, Integer>(currentBlock, 0));
        return nodeId;
    }

    public Long addNode(LevelNode levelNode) {
        Long nodeId = levelNode.getInternalId();
        return addNode(nodeId);
    }

    public ArrayList<Edge> getEdges(Long nodeId) {
        if (cachedEdges.containsKey(nodeId)) {
            return cachedEdges.get(nodeId);
        }
        return null;
    }

    @Override
    public ArrayList<Edge> getEdges(LevelNode levelNode, long creatingTransaction) {
        if (nodeMap.containsKey(levelNode.getInternalId())) {
            return getEdges(levelNode.getExternalId());
        }
        return null;
    }

    @Override
    public void addProperty(LevelNode node, Value properties, int id) {
        addNodeProperty(node.getInternalId(), properties, id);
    }

    public void addNodeProperty(Long nodeId, Value property, int id) {
        nodeProperties.putIfAbsent(nodeId, new HashSet<>());
        nodeProperties.get(nodeId).add(id);
        nodePropertyMap.put(id, property);

    }

    public void addRelationProperty(Long edgeId, Value property, int id) {
        relationshipProperties.putIfAbsent(edgeId, new HashSet<>());
        relationshipProperties.get(edgeId).add(id);
        relationshipPropertyMap.put(id, property);
    }

    @Override
    public void addProperty(Edge edge, Value properties, int id) {
        addRelationProperty(edge.getEdgeid(), properties, id);
    }

    @Override
    public Value getNodeProperty(int propertyId) {
        return this.nodePropertyMap.get(propertyId);
    }

    public boolean hasNodeLabel(Long nodeId, int labelId) {
        return this.nodeLabels.get(nodeId).contains(labelId);
    }

    public void addNodeLabel(Long nodeId, int labelId) {
        nodeLabels.putIfAbsent(nodeId, new HashSet<>());
        nodeLabels.get(nodeId).add(labelId);
    }


    @Override
    public Value getRelationshipProperty(int propertyId) {
        return this.relationshipPropertyMap.get(propertyId);
    }
}
