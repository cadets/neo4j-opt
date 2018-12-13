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

package org.cam.storage.levelgraph.neo4jcompat;

import org.cam.storage.levelgraph.database.NegraphDatabase;
import org.cam.storage.levelgraph.database.NegraphDatabaseBuilder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.security.URLAccessValidationError;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.security.SecurityContext;
import org.neo4j.kernel.impl.coreapi.InternalTransaction;
import org.neo4j.kernel.impl.store.StoreId;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Neo4jGraphDatabaseFacade implements GraphDatabaseAPI {

    NegraphDatabase database;
    StoreId storeId;
    String folder;

    Neo4jGraphDatabaseFacade(String baseFolder) {
        database = getNewDatabase(baseFolder);
        folder = baseFolder;
        storeId = new StoreId(0);
    }

    NegraphDatabase getNewDatabase(String folder) {
        return new NegraphDatabaseBuilder().setBaseFolder(folder).createNegraphDatabase();
    }

    @Override
    public DependencyResolver getDependencyResolver() {
        return null;
    }

    @Override
    public StoreId storeId() {
        return storeId;
    }

    @Override
    public URL validateURLAccess(URL url) throws URLAccessValidationError {
        return null;
    }

    @Override
    public File getStoreDir() {
        return new File(folder);
    }

    @Override
    public InternalTransaction beginTransaction(KernelTransaction.Type type, SecurityContext securityContext) {
        return null;
    }

    @Override
    public InternalTransaction beginTransaction(KernelTransaction.Type type, SecurityContext securityContext, long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Node createNode() {
        return null;
    }

    @Override
    public Long createNodeId() {
        return null;
    }

    @Override
    public Node createNode(Label... labels) {
        return null;
    }

    @Override
    public Node getNodeById(long l) {
        return null;
    }

    @Override
    public Relationship getRelationshipById(long l) {
        return null;
    }

    @Override
    public ResourceIterable<Node> getAllNodes() {
        return null;
    }

    @Override
    public ResourceIterable<Relationship> getAllRelationships() {
        return null;
    }

    @Override
    public ResourceIterator<Node> findNodes(Label label, String s, Object o) {
        return null;
    }

    @Override
    public Node findNode(Label label, String s, Object o) {
        return null;
    }

    @Override
    public ResourceIterator<Node> findNodes(Label label) {
        return null;
    }

    @Override
    public ResourceIterable<Label> getAllLabelsInUse() {
        return null;
    }

    @Override
    public ResourceIterable<RelationshipType> getAllRelationshipTypesInUse() {
        return null;
    }

    @Override
    public ResourceIterable<Label> getAllLabels() {
        return null;
    }

    @Override
    public ResourceIterable<RelationshipType> getAllRelationshipTypes() {
        return null;
    }

    @Override
    public ResourceIterable<String> getAllPropertyKeys() {
        return null;
    }

    @Override
    public boolean isAvailable(long l) {
        return false;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Transaction beginTx() {
        return null;
    }

    @Override
    public Transaction beginTx(long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Result execute(String s) throws QueryExecutionException {
        return null;
    }

    @Override
    public Result execute(String s, long l, TimeUnit timeUnit) throws QueryExecutionException {
        return null;
    }

    @Override
    public Result execute(String s, Map<String, Object> map) throws QueryExecutionException {
        return null;
    }

    @Override
    public Result execute(String s, Map<String, Object> map, long l, TimeUnit timeUnit) throws QueryExecutionException {
        return null;
    }

    @Override
    public <T> TransactionEventHandler<T> registerTransactionEventHandler(TransactionEventHandler<T> transactionEventHandler) {
        return null;
    }

    @Override
    public <T> TransactionEventHandler<T> unregisterTransactionEventHandler(TransactionEventHandler<T> transactionEventHandler) {
        return null;
    }

    @Override
    public KernelEventHandler registerKernelEventHandler(KernelEventHandler kernelEventHandler) {
        return null;
    }

    @Override
    public KernelEventHandler unregisterKernelEventHandler(KernelEventHandler kernelEventHandler) {
        return null;
    }

    @Override
    public Schema schema() {
        return null;
    }

    @Override
    public IndexManager index() {
        return null;
    }

    @Override
    public TraversalDescription traversalDescription() {
        return null;
    }

    @Override
    public BidirectionalTraversalDescription bidirectionalTraversalDescription() {
        return null;
    }
}
