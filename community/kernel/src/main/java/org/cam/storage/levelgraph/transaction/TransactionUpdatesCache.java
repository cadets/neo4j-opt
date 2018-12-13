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

package org.cam.storage.levelgraph.transaction;

import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.datatypes.StorableData;
import org.neo4j.values.storable.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/*
    Transaction cache which holds the individual updates that need to be made to the storage by the transaction.
    Results cache for the transaction is separate from this. That holds the results to be sent upstream.
 */
public class TransactionUpdatesCache {

    HashMap<Integer, Value> relationshipPropertyMap;

    HashMap<Integer, Value> nodePropertyMap;

    HashMap<Integer, String> labelIdToString;

    private ArrayList<Updates> additionList, deletionList;

    private HashMap<Long, StorableData> createNode, createEdge;

    private HashSet<Long> deleteNodes, deleteEdges;

    private HashMap<Long, HashSet<Integer>> nodeLabelAdditions, nodeLabelDeletions,
            nodePropertyAdditions, nodePropertyDeletions,
            relationLabelAdditions, relationLabelDeletions,
            relationPropertyAdditions, relationPropertyDeletions;

    public TransactionUpdatesCache() {
        additionList = new ArrayList<>();
        deletionList = new ArrayList<>();
        createEdge = new HashMap<>();
        createNode = new HashMap<>();
        deleteNodes = new HashSet<>();
        deleteEdges = new HashSet<>();
        nodeLabelAdditions = new HashMap<>();
        nodeLabelDeletions = new HashMap<>();
        nodePropertyAdditions = new HashMap<>();
        nodePropertyDeletions = new HashMap<>();
        relationLabelAdditions = new HashMap<>();
        relationPropertyDeletions = new HashMap<>();
        relationLabelDeletions = new HashMap<>();
        relationPropertyAdditions = new HashMap<>();
        relationshipPropertyMap = new HashMap<>();
        nodePropertyMap = new HashMap<>();
        labelIdToString = new HashMap<>();
    }

    public void clearCache() {
        additionList.clear();
        deletionList.clear();
        createEdge.clear();
        nodeLabelDeletions.clear();
        createNode.clear();
        nodePropertyAdditions.clear();
        nodePropertyDeletions.clear();
        nodeLabelAdditions.clear();
        relationLabelAdditions.clear();
        relationLabelDeletions.clear();
        relationPropertyAdditions.clear();
        relationPropertyDeletions.clear();


    }

    void addUpdateHashMap(HashMap<Long, HashSet<Integer>> store, Long id, int data) {
        store.putIfAbsent(id, new HashSet<>());
        store.get(id).add(data);

    }

    Long nodeGetInternalId(StorableData data) {
        return ((LevelNode) data).getInternalId();
    }

    public Value getNodeProperty(int id) {
        return nodePropertyMap.get(id);
    }

    public Value getRelationshipProperty(int id) {
        return relationshipPropertyMap.get(id);
    }
    public Value setNodeProperty(int id, Value v){
        nodePropertyMap.put(id,v);
        return v;
    }
    public Value setRelationProperty(int id, Value v){
        relationshipPropertyMap.put(id,v);
        return v;
    }
    private boolean collectionHasData(HashMap<Long, HashSet<Integer>> map1, long var1, int var2) {
        if (map1.containsKey(var1)) {
            return map1.get(var1).contains(var2);
        } else {
            return false;
        }
    }

    private void addCollectionData(HashMap<Long, HashSet<Integer>> map1, long var1, int var2) {
        map1.putIfAbsent(var1, new HashSet<>());
        map1.get(var1).add(var2);
    }

    private boolean removeCollectionData(HashMap<Long, HashSet<Integer>> map1, long var1, int var2) {
        if (map1.containsKey(var1)) {
            if (map1.get(var1).contains(var2)) {
                map1.get(var1).remove(var2);
                return true;
            }
        }
        return false;
    }

    public boolean hasNode(long nodeId) {
        return createNode.containsKey(nodeId);
    }

    public boolean hasEdge(long edgeId) {
        return createEdge.containsKey(edgeId);
    }

    public boolean nodeHasProperty(long nodeId, int propertyId) {
        return collectionHasData(nodePropertyAdditions, nodeId, propertyId);
    }

    public boolean nodeHasLabel(long nodeId, int propertyId) {
        return collectionHasData(nodeLabelAdditions, nodeId, propertyId);
    }

    public boolean relationshipHasProperty(long nodeId, int propertyId) {
        return collectionHasData(relationPropertyAdditions, nodeId, propertyId);
    }

    public boolean relationshipHasLabel(long nodeId, int propertyId) {
        return collectionHasData(relationLabelAdditions, nodeId, propertyId);
    }

    public void addCreateUpdate(UpdateTarget target, StorableData data, Long point, int id) {
        switch (target) {
            case NodeUpdate:
                createNode.put(point, data);
                break;
            case EdgeUpdate:
                createEdge.put(point, data);
                break;
            case EdgePropertyUpdate:
                if (!removeCollectionData(relationPropertyDeletions, point, id)) {
                    addUpdateHashMap(relationPropertyAdditions, point, id);
                }
                break;
            case EdgeLabelUpdate:
                if (!removeCollectionData(relationLabelDeletions, point, id))
                    addUpdateHashMap(relationLabelAdditions, point, id);
                break;
            case NodeLabelUpdate:
                if (!removeCollectionData(nodeLabelDeletions, point, id))
                    addUpdateHashMap(nodeLabelAdditions, point, id);
                break;
            case NodePropertyUpdate:
                if (!removeCollectionData(nodePropertyDeletions, point, id))
                    addUpdateHashMap(nodePropertyAdditions, point, id);
                break;
        }
    }

    public void addDeletionUpdate(UpdateTarget target, Long point, int id) {
        switch (target) {
            case NodeUpdate:
                if (createNode.containsKey(point)) {
                    createNode.remove(point);
                } else {
                    deleteNodes.add(point);
                }
                break;
            case EdgeUpdate:
                if (createEdge.containsKey(point)) {
                    createEdge.remove(point);
                } else
                    deleteEdges.add(point);
            case EdgePropertyUpdate:
                if (!removeCollectionData(relationPropertyAdditions, point, id)) {
                    addUpdateHashMap(relationPropertyDeletions, point, id);
                }
                break;
            case EdgeLabelUpdate:
                if (!removeCollectionData(relationLabelAdditions, point, id))
                    addUpdateHashMap(relationLabelDeletions, point, id);
                break;
            case NodeLabelUpdate:
                if (!removeCollectionData(nodeLabelAdditions, point, id))
                    addUpdateHashMap(nodeLabelDeletions, point, id);
                break;
            case NodePropertyUpdate:
                if (!removeCollectionData(nodePropertyAdditions, point, id))
                    addUpdateHashMap(nodePropertyDeletions, point, id);
                break;
        }
    }

    void addUpdate(UpdateType updateType, UpdateTarget target, StorableData data, int id) {
        Updates updates = new Updates(updateType, target, data);
        switch (updateType) {
            case Update:
                additionList.add(updates);
                break;
            case Delete:
                deletionList.add(updates);
                addDeletionUpdate(target, data.getDataId(), id);
                break;
            case Create:
                additionList.add(updates);
                addCreateUpdate(target, data, data.getDataId(), id);
                break;
            default:
                additionList.add(updates);
        }
    }

    public HashMap<Long, StorableData> getCreatedNodes() {
        return createNode;
    }

    public HashMap<Long, StorableData> getCreatedEdges() {
        return createEdge;
    }

    public HashSet<Long> getDeleteNodes() {
        return deleteNodes;
    }

    public HashSet<Long> getDeleteEdges() {
        return deleteEdges;
    }

    public HashMap<Long, HashSet<Integer>> getNodeLabelAdditions() {
        return nodeLabelAdditions;
    }

    public HashMap<Long, HashSet<Integer>> getNodeLabelDeletions() {
        return nodeLabelDeletions;
    }

    public HashMap<Long, HashSet<Integer>> getNodePropertyAdditions() {
        return nodePropertyAdditions;
    }

    public HashMap<Long, HashSet<Integer>> getNodePropertyDeletions() {
        return nodePropertyDeletions;
    }

    public HashMap<Long, HashSet<Integer>> getRelationLabelAdditions() {
        return relationLabelAdditions;
    }

    public HashMap<Long, HashSet<Integer>> getRelationLabelDeletions() {
        return relationLabelDeletions;
    }

    public HashMap<Long, HashSet<Integer>> getRelationPropertyAdditions() {
        return relationPropertyAdditions;
    }

    public HashMap<Long, HashSet<Integer>> getRelationPropertyDeletions() {
        return relationPropertyDeletions;
    }

    public ArrayList<Updates> getDeletionList() {
        return deletionList;
    }

    public ArrayList<Updates> getAdditionList() {
        return additionList;
    }

    //Having additions and deletions separately would make life easier.
    public class Updates {
        UpdateType updateType;
        UpdateTarget updateTarget;
        StorableData data;

        Updates(UpdateType updateType, UpdateTarget updateTarget, StorableData storableData) { //TODO Convert result to something meaningful and query to a deserialised format
            this.updateType = updateType;
            this.updateTarget = updateTarget;
            data = storableData;
        }

        public UpdateType getUpdateType() {
            return updateType;
        }

        public UpdateTarget getUpdateTarget() {
            return updateTarget;
        }

        public StorableData getData() {
            return data;
        }

        private void getUpdateTypesFromQuery(String query) {
            updateType = UpdateType.Create;
            updateTarget = UpdateTarget.EdgeUpdate;
        }

        byte[] storableUpdate() {
            byte[] dataInBytes = data.toBytes();
            byte[] result = new byte[2 + dataInBytes.length];
            result[0] = updateType.toByte(updateType);
            result[1] = updateTarget.toByte(updateTarget);
            System.arraycopy(dataInBytes, 0, result, 2, dataInBytes.length);
            return result;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Updates)) return false;

            Updates updates = (Updates) o;

            return data.equals(updates.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }
    }

}
