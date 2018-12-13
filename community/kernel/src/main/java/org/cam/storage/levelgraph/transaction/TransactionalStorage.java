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

import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.datatypes.StorableData;
import org.cam.storage.levelgraph.results.MultiEdgeResult;
import org.cam.storage.levelgraph.storage.IdGenerator;
import org.cam.storage.levelgraph.storage.dataqueue.WriteAheadLog;
import org.cam.storage.levelgraph.storage.ondiskstorage.UnifiedDataStorage;
import org.neo4j.collection.primitive.PrimitiveIntIterator;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.graphdb.Direction;
import org.neo4j.kernel.impl.api.store.RelationshipIterator;
import org.neo4j.storageengine.api.Token;
import org.neo4j.values.storable.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static org.cam.storage.levelgraph.transaction.UpdateTarget.*;
import static org.cam.storage.levelgraph.transaction.UpdateType.Create;

/**
 * Unified interface for all transaction related data Operations
 *
 */
public class TransactionalStorage implements TransactionStorageInterface {

    TransactionUpdatesCache transactionUpdatesCache;

    TransactionsResultsCache transactionsResultsCache;

    UnifiedDataStorage storage;

    TransactionInterface transaction;

    WriteAheadLog wal;


    private TransactionIdProvider nodeIdProvider;

    private TransactionIdProvider relationIdProvider;
    /**
     *
     */
    public TransactionalStorage(IdGenerator nodeidGenerator, IdGenerator relationIdGenerator) {
        nodeIdProvider = new TransactionIdProvider(nodeidGenerator);
        relationIdProvider = new TransactionIdProvider(relationIdGenerator);
        transactionUpdatesCache = new TransactionUpdatesCache();
        transactionsResultsCache = new TransactionsResultsCache();
    }

    public TransactionalStorage(UnifiedDataStorage storage, WriteAheadLog wal) {
        transactionUpdatesCache = new TransactionUpdatesCache();
        transactionsResultsCache = new TransactionsResultsCache();
        this.storage = storage;
        this.wal = wal;
    }

    public boolean validNode(long nodeId) {
        if (transactionUpdatesCache.hasNode(nodeId)) {
            return true;
        }
        return storage.hasNode(nodeId);
    }

    public boolean validEdge(long edgeId) {
        if (transactionUpdatesCache.hasEdge(edgeId)) {
            return true;
        }
        return storage.hasEdge(edgeId);
    }
    public void setStorage(UnifiedDataStorage storage) {
        this.storage = storage;
    }

    public void setWal(WriteAheadLog wal) {
        this.wal = wal;
    }

    @Override
    public Long addEdge(Edge edge) {
        transactionUpdatesCache.addUpdate(Create, EdgeUpdate, edge, 0);
        transactionsResultsCache.addResult(edge);
        edge.setEdgeid(relationIdProvider.getId());
        return edge.getEdgeid();
    }

    @Override
    public Long addNode(LevelNode levelNode) {
        transactionUpdatesCache.addUpdate(Create, UpdateTarget.NodeUpdate, levelNode, 0);
        transactionsResultsCache.addResult(levelNode);
        return levelNode.getInternalId();
    }

    public Long relationshipCreate(int i, long l, long l1) {
        Edge edge = new Edge(l, l1, i);
        return addEdge(edge);
    }

    public Long nodeCreate() {
        LevelNode node = new LevelNode();
        node.setInternalId(nodeIdProvider.getId());
        return addNode(node);
    }


    public void nodeDelete(long l) {
        transactionUpdatesCache.addDeletionUpdate(UpdateTarget.NodeUpdate, l, 0);
    }

    public int nodeDetachDelete(long l) {

        nodeDelete(l);
        //What to do?
        return 0;
    }


    public void relationshipDelete(long l) {
        transactionUpdatesCache.addDeletionUpdate(EdgeUpdate, l, 0);
    }

    public boolean nodeAddLabel(long l, int i) {
        transactionUpdatesCache.addCreateUpdate(NodeLabelUpdate,null, l, i);
        return false;
    }

    public boolean nodeRemoveLabel(long l, int i) {
        boolean returnValue = false;
        if (transactionUpdatesCache.nodeHasLabel(l, i)) {
            returnValue = true;
        }
        transactionUpdatesCache.addDeletionUpdate(NodeLabelUpdate, l, i);

        return returnValue;

    }

    public Value nodeSetProperty(long l, int i, Value value) {
        transactionUpdatesCache.addCreateUpdate(NodePropertyUpdate,null, l, i);
        transactionUpdatesCache.setNodeProperty(i,value);
        return value;
    }

    public Value relationshipSetProperty(long l, int i, Value value) {
        transactionUpdatesCache.addCreateUpdate(EdgePropertyUpdate,null, l, i);
        transactionUpdatesCache.setRelationProperty(i,value);
        return value;
    }

    public Value graphSetProperty(int i, Value value) {
        return null;
    }

    public Value nodeRemoveProperty(long l, int i) {
        Value returnValue;
        if (transactionUpdatesCache.nodeHasProperty(l, i)) {
            returnValue = transactionUpdatesCache.getNodeProperty(i);
            transactionUpdatesCache.addDeletionUpdate(NodePropertyUpdate, l, i);
            return returnValue;
        }
        transactionUpdatesCache.addDeletionUpdate(NodePropertyUpdate, l, i);
        returnValue = storage.getNodeProperty(i);
        return returnValue;
    }

    public Value relationshipRemoveProperty(long l, int i) {
        Value returnValue;
        if (transactionUpdatesCache.relationshipHasProperty(l, i)) {
            returnValue = transactionUpdatesCache.getRelationshipProperty(i);
            transactionUpdatesCache.addDeletionUpdate(EdgePropertyUpdate, l,i);
            return returnValue;
        }
        transactionUpdatesCache.addDeletionUpdate(EdgePropertyUpdate, l,i);
        return null;
    }

    public Value graphRemoveProperty(int i) {
        return null;
    }


    public Map<String, Object> getMetaData() {
        return null;
    }

    public void setMetaData(Map<String, Object> map) {

    }


    public int labelGetForName(String s) {
        return 0;
    }

    public String labelGetName(int i) {
        return null;
    }

    public Iterator<Token> labelsGetAllTokens() {
        return null;
    }

    public int propertyKeyGetForName(String s) {
        return 0;
    }

    public String propertyKeyGetName(int i) {
        return null;
    }

    public Iterator<Token> propertyKeyGetAllTokens() {
        return null;
    }

    public int relationshipTypeGetForName(String s) {
        return 0;
    }

    public String relationshipTypeGetName(int i) {
        return null;
    }

    public Iterator<Token> relationshipTypesGetAllTokens() {
        return null;
    }

    public int labelCount() {
        return 0;
    }

    public int propertyKeyCount() {
        return 0;
    }

    public int relationshipTypeCount() {
        return 0;
    }

    public PrimitiveLongIterator nodesGetForLabel(int i) {
        return null;
    }


    public PrimitiveLongIterator nodesGetAll() {
        return null;
    }

    public PrimitiveLongIterator relationshipsGetAll() {
        return null;
    }

    public RelationshipIterator nodeGetRelationships(long l, Direction direction, int[] ints) {
        return null;
    }

    public RelationshipIterator nodeGetRelationships(long l, Direction direction) {
        return null;
    }

    public boolean nodeExists(long l) {
        return false;
    }

    public boolean nodeHasLabel(long l, int i) {
        return false;
    }

    public int nodeGetDegree(long l, Direction direction, int i) {
        return 0;
    }

    public int nodeGetDegree(long l, Direction direction) {
        return 0;
    }

    public boolean nodeIsDense(long l) {
        return false;
    }

    public PrimitiveIntIterator nodeGetLabels(long l) {
        return null;
    }

    public PrimitiveIntIterator nodeGetPropertyKeys(long l) {
        return null;
    }

    public PrimitiveIntIterator relationshipGetPropertyKeys(long l) {
        return null;
    }

    public PrimitiveIntIterator graphGetPropertyKeys() {
        return null;
    }

    public PrimitiveIntIterator nodeGetRelationshipTypes(long l) {
        return null;
    }

    public boolean nodeHasProperty(long l, int i) {
        return false;
    }

    public Value nodeGetProperty(long l, int i) {
        return null;
    }

    public boolean relationshipHasProperty(long l, int i) {
        return false;
    }

    public Value relationshipGetProperty(long l, int i) {
        return null;
    }

    public boolean graphHasProperty(int i) {
        return false;
    }

    public Value graphGetProperty(int i) {
        return null;
    }


    public long nodesGetCount() {
        return 0;
    }

    public long relationshipsGetCount() {
        return 0;
    }

    public long countsForNode(int i) {
        return 0;
    }

    public long countsForNodeWithoutTxState(int i) {
        return 0;
    }

    public long countsForRelationship(int i, int i1, int i2) {
        return 0;
    }

    public long countsForRelationshipWithoutTxState(int i, int i1, int i2) {
        return 0;
    }

    public int labelGetOrCreateForName(String s) {
        return 0;
    }

    public int propertyKeyGetOrCreateForName(String s) {
        return 0;
    }

    public int relationshipTypeGetOrCreateForName(String s) {
        return 0;
    }

    public void labelCreateForName(String s, int i) {

    }

    public void propertyKeyCreateForName(String s, int i) {

    }

    public void relationshipTypeCreateForName(String s, int i) {

    }



    //Need to add a conditional read edge etc.
    @Override
    public StorableData getEdges(LevelNode levelNode) {
        transactionsResultsCache.addResult(new MultiEdgeResult(storage.getEdges(levelNode, transaction.getTransactionId())));
        return null;
    }

    @Override
    public void deleteNode(LevelNode levelNode) {
        transactionUpdatesCache.addUpdate(UpdateType.Delete, UpdateTarget.NodeUpdate, levelNode, 0);
    }

    @Override
    public LevelNode findNode(LevelNode levelNode) {

        LevelNode temp = storage.getNode(levelNode);
        if (temp != null) {
            levelNode = temp;
        } else {
            temp = (LevelNode) transactionUpdatesCache.getCreatedNodes().get(levelNode.getInternalId());
            if (temp != null)
                levelNode = temp;
//            temp=transactionUpdatesCache.;
        }
        return levelNode;

//TODO
    }

    @Override
    public void deleteEdge(Edge edge) {
        transactionUpdatesCache.addUpdate(UpdateType.Delete, EdgeUpdate, edge, 0);
    }

    @Override
    public Long getTransactionId() {
        return transaction.getTransactionId();
    }

    public ArrayList<StorableData> getResults() {
        return transactionsResultsCache.getResults();
    }

    public void setTransaction(TransactionInterface transaction) {
        this.transaction = transaction;
    }

    public void commit() {

        ArrayList<TransactionUpdatesCache.Updates> adds = transactionUpdatesCache.getAdditionList();

        ArrayList<TransactionUpdatesCache.Updates> deletes = transactionUpdatesCache.getDeletionList();

        for (TransactionUpdatesCache.Updates update : adds) {
            //Addition and deletions only handled here, TODO handle updates
            StorableData data = update.getData();
            if (data instanceof LevelNode) {
                wal.addNode((LevelNode) data);
                storage.addNode((LevelNode) data);
            }
            if (data instanceof Edge) {
                wal.addEdge((Edge) data);
                storage.addEdge((Edge) update.getData());
            }
        }
        for (TransactionUpdatesCache.Updates update : deletes) {
            StorableData data = update.getData();
            if (data instanceof LevelNode) {
                wal.deleteNode((LevelNode) data);
                storage.deleteNode((LevelNode) data, getTransactionId());
            }
            if (data instanceof Edge) {
                wal.deleteEdge((Edge) data);
                storage.deleteEdge((Edge) data, getTransactionId());
            }
        }
        transactionUpdatesCache.clearCache();
    }

}
