/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.kernel.impl.api;

import org.neo4j.collection.RawIterator;
import org.neo4j.collection.primitive.PrimitiveIntIterator;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.cursor.Cursor;
import org.neo4j.graphdb.Direction;
import org.neo4j.io.pagecache.tracing.cursor.PageCursorTracer;
import org.neo4j.kernel.api.*;
import org.neo4j.kernel.api.exceptions.*;
import org.neo4j.kernel.api.exceptions.explicitindex.AutoIndexingKernelException;
import org.neo4j.kernel.api.exceptions.explicitindex.ExplicitIndexNotFoundKernelException;
import org.neo4j.kernel.api.exceptions.index.IndexNotApplicableKernelException;
import org.neo4j.kernel.api.exceptions.index.IndexNotFoundKernelException;
import org.neo4j.kernel.api.exceptions.schema.*;
import org.neo4j.kernel.api.index.InternalIndexState;
import org.neo4j.kernel.api.index.SchemaIndexProvider;
import org.neo4j.kernel.api.proc.CallableUserAggregationFunction;
import org.neo4j.kernel.api.proc.ProcedureSignature;
import org.neo4j.kernel.api.proc.QualifiedName;
import org.neo4j.kernel.api.proc.UserFunctionSignature;
import org.neo4j.kernel.api.query.ExecutingQuery;
import org.neo4j.kernel.api.schema.IndexQuery;
import org.neo4j.kernel.api.schema.LabelSchemaDescriptor;
import org.neo4j.kernel.api.schema.RelationTypeSchemaDescriptor;
import org.neo4j.kernel.api.schema.SchemaDescriptor;
import org.neo4j.kernel.api.schema.constaints.*;
import org.neo4j.kernel.api.schema.index.IndexDescriptor;
import org.neo4j.kernel.impl.api.operations.KeyReadOperations;
import org.neo4j.kernel.impl.api.operations.KeyWriteOperations;
import org.neo4j.kernel.impl.api.operations.SchemaWriteOperations;
import org.neo4j.kernel.impl.api.store.RelationshipIterator;
import org.neo4j.kernel.impl.query.clientconnection.ClientConnectionInfo;
import org.neo4j.register.Register;
import org.neo4j.storageengine.api.NodeItem;
import org.neo4j.storageengine.api.PropertyItem;
import org.neo4j.storageengine.api.RelationshipItem;
import org.neo4j.storageengine.api.Token;
import org.neo4j.storageengine.api.lock.ResourceType;
import org.neo4j.storageengine.api.schema.PopulationProgress;
import org.neo4j.values.storable.Value;
import org.neo4j.values.virtual.MapValue;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class ShortCircuitOperationsFacade implements ReadOperations, DataWriteOperations, TokenWriteOperations, SchemaWriteOperations,
        QueryRegistryOperations, ProcedureCallOperations, ExecutionStatisticsOperations{

    public ShortCircuitOperationsFacade() {
    }

    @Override
    public long nodeCreate() {
        return 0;
    }

    @Override
    public void nodeDelete(long nodeId) throws EntityNotFoundException, InvalidTransactionTypeKernelException, AutoIndexingKernelException {

    }

    @Override
    public int nodeDetachDelete(long nodeId) throws KernelException {
        return 0;
    }

    @Override
    public long relationshipCreate(int relationshipTypeId, long startNodeId, long endNodeId) throws RelationshipTypeIdNotFoundKernelException, EntityNotFoundException {
        return 0;
    }

    @Override
    public void relationshipDelete(long relationshipId) throws EntityNotFoundException, InvalidTransactionTypeKernelException, AutoIndexingKernelException {

    }

    /**
     * Labels a node with the label corresponding to the given label id.
     * If the node already had that label nothing will happen. Label ids are retrieved from
     * {@link KeyWriteOperations#labelGetOrCreateForName(Statement, String)}
     * or {@link
     * KeyReadOperations#labelGetForName(Statement, String)}.
     *
     * @param nodeId
     * @param labelId
     */
    @Override
    public boolean nodeAddLabel(long nodeId, int labelId) throws EntityNotFoundException, ConstraintValidationException {
        return false;
    }

    /**
     * Removes a label with the corresponding id from a node.
     * If the node doesn't have that label nothing will happen. Label id are retrieved from
     * {@link KeyWriteOperations#labelGetOrCreateForName(Statement, String)}
     * or {@link
     * KeyReadOperations#labelGetForName(Statement, String)}.
     *
     * @param nodeId
     * @param labelId
     */
    @Override
    public boolean nodeRemoveLabel(long nodeId, int labelId) throws EntityNotFoundException {
        return false;
    }

    @Override
    public Value nodeSetProperty(long nodeId, int propertyKeyId, Value value) throws EntityNotFoundException, AutoIndexingKernelException, InvalidTransactionTypeKernelException, ConstraintValidationException {
        return null;
    }

    @Override
    public Value relationshipSetProperty(long relationshipId, int propertyKeyId, Value value) throws EntityNotFoundException, AutoIndexingKernelException, InvalidTransactionTypeKernelException {
        return null;
    }

    @Override
    public Value graphSetProperty(int propertyKeyId, Value value) {
        return null;
    }

    /**
     * Remove a node's property given the node's id and the property key id and return the value to which
     * it was set or null if it was not set on the node
     *
     * @param nodeId
     * @param propertyKeyId
     */
    @Override
    public Value nodeRemoveProperty(long nodeId, int propertyKeyId) throws EntityNotFoundException, AutoIndexingKernelException, InvalidTransactionTypeKernelException {
        return null;
    }

    @Override
    public Value relationshipRemoveProperty(long relationshipId, int propertyKeyId) throws EntityNotFoundException, AutoIndexingKernelException, InvalidTransactionTypeKernelException {
        return null;
    }

    @Override
    public Value graphRemoveProperty(int propertyKeyId) {
        return null;
    }

    /**
     * Creates an explicit index in a separate transaction if not yet available.
     *
     * @param indexName
     * @param customConfig
     */
    @Override
    public void nodeExplicitIndexCreateLazily(String indexName, Map<String, String> customConfig) {

    }

    @Override
    public void nodeExplicitIndexCreate(String indexName, Map<String, String> customConfig) {

    }

    /**
     * Creates an explicit index in a separate transaction if not yet available.
     *
     * @param indexName
     * @param customConfig
     */
    @Override
    public void relationshipExplicitIndexCreateLazily(String indexName, Map<String, String> customConfig) {

    }

    @Override
    public void relationshipExplicitIndexCreate(String indexName, Map<String, String> customConfig) {

    }

    @Override
    public String nodeExplicitIndexSetConfiguration(String indexName, String key, String value) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public String relationshipExplicitIndexSetConfiguration(String indexName, String key, String value) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public String nodeExplicitIndexRemoveConfiguration(String indexName, String key) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public String relationshipExplicitIndexRemoveConfiguration(String indexName, String key) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public void nodeAddToExplicitIndex(String indexName, long node, String key, Object value) throws EntityNotFoundException, ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void nodeRemoveFromExplicitIndex(String indexName, long node, String key, Object value) throws ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void nodeRemoveFromExplicitIndex(String indexName, long node, String key) throws ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void nodeRemoveFromExplicitIndex(String indexName, long node) throws ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void relationshipAddToExplicitIndex(String indexName, long relationship, String key, Object value) throws EntityNotFoundException, ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void relationshipRemoveFromExplicitIndex(String indexName, long relationship, String key, Object value) throws ExplicitIndexNotFoundKernelException, EntityNotFoundException {

    }

    @Override
    public void relationshipRemoveFromExplicitIndex(String indexName, long relationship, String key) throws ExplicitIndexNotFoundKernelException, EntityNotFoundException {

    }

    @Override
    public void relationshipRemoveFromExplicitIndex(String indexName, long relationship) throws ExplicitIndexNotFoundKernelException, EntityNotFoundException {

    }

    @Override
    public void nodeExplicitIndexDrop(String indexName) throws ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void relationshipExplicitIndexDrop(String indexName) throws ExplicitIndexNotFoundKernelException {

    }

    /**
     * @return provide current page cursor tracer that expose current transaction page cache statistic
     */
    @Override
    public PageCursorTracer getPageCursorTracer() {
        return null;
    }

    /**
     * Invoke a read-only procedure by name.
     *
     * @param name      the name of the procedure.
     * @param arguments the procedure arguments.
     * @return an iterator containing the procedure results.
     * @throws ProcedureException if there was an exception thrown during procedure execution.
     */
    @Override
    public RawIterator<Object[], ProcedureException> procedureCallRead(QualifiedName name, Object[] arguments) throws ProcedureException {
        return null;
    }

    /**
     * Invoke a read-only procedure by name, and set the transaction's access mode to
     * {@link AccessMode.Static#READ READ} for the duration of the procedure execution.
     *
     * @param name      the name of the procedure.
     * @param arguments the procedure arguments.
     * @return an iterator containing the procedure results.
     * @throws ProcedureException if there was an exception thrown during procedure execution.
     */
    @Override
    public RawIterator<Object[], ProcedureException> procedureCallReadOverride(QualifiedName name, Object[] arguments) throws ProcedureException {
        return null;
    }

    /**
     * Invoke a read/write procedure by name.
     *
     * @param name      the name of the procedure.
     * @param arguments the procedure arguments.
     * @return an iterator containing the procedure results.
     * @throws ProcedureException if there was an exception thrown during procedure execution.
     */
    @Override
    public RawIterator<Object[], ProcedureException> procedureCallWrite(QualifiedName name, Object[] arguments) throws ProcedureException {
        return null;
    }

    /**
     * Invoke a read/write procedure by name, and set the transaction's access mode to
     * {@link AccessMode.Static#WRITE WRITE} for the duration of the procedure execution.
     *
     * @param name      the name of the procedure.
     * @param arguments the procedure arguments.
     * @return an iterator containing the procedure results.
     * @throws ProcedureException if there was an exception thrown during procedure execution.
     */
    @Override
    public RawIterator<Object[], ProcedureException> procedureCallWriteOverride(QualifiedName name, Object[] arguments) throws ProcedureException {
        return null;
    }

    /**
     * Invoke a schema write procedure by name.
     *
     * @param name      the name of the procedure.
     * @param arguments the procedure arguments.
     * @return an iterator containing the procedure results.
     * @throws ProcedureException if there was an exception thrown during procedure execution.
     */
    @Override
    public RawIterator<Object[], ProcedureException> procedureCallSchema(QualifiedName name, Object[] arguments) throws ProcedureException {
        return null;
    }

    /**
     * Invoke a schema write procedure by name, and set the transaction's access mode to
     * {@link AccessMode.Static#FULL FULL} for the duration of the procedure execution.
     *
     * @param name      the name of the procedure.
     * @param arguments the procedure arguments.
     * @return an iterator containing the procedure results.
     * @throws ProcedureException if there was an exception thrown during procedure execution.
     */
    @Override
    public RawIterator<Object[], ProcedureException> procedureCallSchemaOverride(QualifiedName name, Object[] arguments) throws ProcedureException {
        return null;
    }

    /**
     * Invoke a read-only function by name
     *
     * @param name      the name of the function.
     * @param arguments the function arguments.
     * @throws ProcedureException if there was an exception thrown during function execution.
     */
    @Override
    public Object functionCall(QualifiedName name, Object[] arguments) throws ProcedureException {
        return null;
    }

    /**
     * Invoke a read-only function by name, and set the transaction's access mode to
     * {@link AccessMode.Static#READ READ} for the duration of the function execution.
     *
     * @param name      the name of the function.
     * @param arguments the function arguments.
     * @throws ProcedureException if there was an exception thrown during function execution.
     */
    @Override
    public Object functionCallOverride(QualifiedName name, Object[] arguments) throws ProcedureException {
        return null;
    }

    /**
     * Create a read-only aggregation function by name
     *
     * @param name the name of the function
     * @return the aggregation function
     * @throws ProcedureException
     */
    @Override
    public CallableUserAggregationFunction.Aggregator aggregationFunction(QualifiedName name) throws ProcedureException {
        return null;
    }

    /**
     * Invoke a read-only aggregation function by name, and set the transaction's access mode to
     * {@link AccessMode.Static#READ READ} for the duration of the function execution.
     *
     * @param name the name of the function.
     * @throws ProcedureException if there was an exception thrown during function execution.
     */
    @Override
    public CallableUserAggregationFunction.Aggregator aggregationFunctionOverride(QualifiedName name) throws ProcedureException {
        return null;
    }

    /**
     * Sets the user defined meta data to be associated with started queries.
     *
     * @param data the meta data
     */
    @Override
    public void setMetaData(Map<String, Object> data) {

    }

    /**
     * Gets associated meta data.
     *
     * @return the meta data
     */
    @Override
    public Map<String, Object> getMetaData() {
        return null;
    }

    /**
     * List of all currently running stream in this transaction. An user can have multiple stream running
     * simultaneously on the same transaction.
     */
    @Override
    public Stream<ExecutingQuery> executingQueries() {
        return null;
    }

    /**
     * Registers a query, and creates the ExecutingQuery object for it.
     *
     * @param descriptor
     * @param queryText
     * @param queryParameters
     */
    @Override
    public ExecutingQuery startQueryExecution(ClientConnectionInfo descriptor, String queryText, MapValue queryParameters) {
        return null;
    }

    /**
     * Registers an already known query to a this transaction.
     * <p>
     * This is used solely for supporting PERIODIC COMMIT which requires committing and starting new transactions
     * and associating the same ExecutingQuery with those new transactions.
     *
     * @param executingQuery
     */
    @Override
    public void registerExecutingQuery(ExecutingQuery executingQuery) {

    }

    /**
     * Disassociates a query with this transaction.
     *
     * @param executingQuery
     */
    @Override
    public void unregisterExecutingQuery(ExecutingQuery executingQuery) {

    }

    /**
     * Returns a label id for a label name. If the label doesn't exist, {@link KeyReadOperations#NO_SUCH_LABEL}
     * will be returned.
     *
     * @param labelName
     */
    @Override
    public int labelGetForName(String labelName) {
        return 0;
    }

    /**
     * Returns the label name for the given label id.
     *
     * @param labelId
     */
    @Override
    public String labelGetName(int labelId) throws LabelNotFoundKernelException {
        return null;
    }

    /**
     * Returns the labels currently stored in the database *
     */
    @Override
    public Iterator<Token> labelsGetAllTokens() {
        return null;
    }

    /**
     * Returns a property key id for the given property key. If the property key doesn't exist,
     * {@link StatementConstants#NO_SUCH_PROPERTY_KEY} will be returned.
     *
     * @param propertyKeyName
     */
    @Override
    public int propertyKeyGetForName(String propertyKeyName) {
        return 0;
    }

    /**
     * Returns the name of a property given its property key id
     *
     * @param propertyKeyId
     */
    @Override
    public String propertyKeyGetName(int propertyKeyId) throws PropertyKeyIdNotFoundKernelException {
        return null;
    }

    /**
     * Returns the property keys currently stored in the database
     */
    @Override
    public Iterator<Token> propertyKeyGetAllTokens() {
        return null;
    }

    @Override
    public int relationshipTypeGetForName(String relationshipTypeName) {
        return 0;
    }

    @Override
    public String relationshipTypeGetName(int relationshipTypeId) throws RelationshipTypeIdNotFoundKernelException {
        return null;
    }

    /**
     * Returns the relationship types currently stored in the database
     */
    @Override
    public Iterator<Token> relationshipTypesGetAllTokens() {
        return null;
    }

    @Override
    public int labelCount() {
        return 0;
    }

    @Override
    public int propertyKeyCount() {
        return 0;
    }

    @Override
    public int relationshipTypeCount() {
        return 0;
    }

    /**
     * @param labelId the label id of the label that returned nodes are guaranteed to have
     * @return ids of all nodes that have the given label
     */
    @Override
    public PrimitiveLongIterator nodesGetForLabel(int labelId) {
        return null;
    }

    /**
     * Queries the given index with the given index query.
     *
     * @param index      the index to query against.
     * @param predicates array of the {@link IndexQuery} predicates to query for.
     * @return ids of the matching nodes
     * @throws IndexNotFoundKernelException if no such index is found.
     */
    @Override
    public PrimitiveLongIterator indexQuery(IndexDescriptor index, IndexQuery... predicates) throws IndexNotFoundKernelException, IndexNotApplicableKernelException {
        return null;
    }

    /**
     * @return an iterator over all nodes in the database.
     */
    @Override
    public PrimitiveLongIterator nodesGetAll() {
        return null;
    }

    /**
     * @return an iterator over all relationships in the database.
     */
    @Override
    public PrimitiveLongIterator relationshipsGetAll() {
        return null;
    }

    @Override
    public RelationshipIterator nodeGetRelationships(long nodeId, Direction direction, int[] relTypes) throws EntityNotFoundException {
        return null;
    }

    @Override
    public RelationshipIterator nodeGetRelationships(long nodeId, Direction direction) throws EntityNotFoundException {
        return null;
    }

    /**
     * Returns node id of unique node found in the given unique index for value or
     * {@link StatementConstants#NO_SUCH_NODE} if the index does not contain a
     * matching node.
     * <p/>
     * If a node is found, a READ lock for the index entry will be held. If no node
     * is found (if {@link StatementConstants#NO_SUCH_NODE} was returned), a WRITE
     * lock for the index entry will be held. This is to facilitate unique creation
     * of nodes, to build get-or-create semantics on top of this method.
     *
     * @param index
     * @param predicates
     * @throws IndexNotFoundKernelException if no such index found.
     */
    @Override
    public long nodeGetFromUniqueIndexSeek(IndexDescriptor index, IndexQuery.ExactPredicate... predicates) throws IndexNotFoundKernelException, IndexBrokenKernelException, IndexNotApplicableKernelException {
        return 0;
    }

    @Override
    public long nodesCountIndexed(IndexDescriptor index, long nodeId, Value value) throws IndexNotFoundKernelException, IndexBrokenKernelException {
        return 0;
    }

    @Override
    public boolean nodeExists(long nodeId) {
        return false;
    }

    /**
     * Checks if a node is labeled with a certain label or not. Returns
     * {@code true} if the node is labeled with the label, otherwise {@code false.}
     *
     * @param nodeId
     * @param labelId
     */
    @Override
    public boolean nodeHasLabel(long nodeId, int labelId) throws EntityNotFoundException {
        return false;
    }

    @Override
    public int nodeGetDegree(long nodeId, Direction direction, int relType) throws EntityNotFoundException {
        return 0;
    }

    @Override
    public int nodeGetDegree(long nodeId, Direction direction) throws EntityNotFoundException {
        return 0;
    }

    @Override
    public boolean nodeIsDense(long nodeId) throws EntityNotFoundException {
        return false;
    }

    /**
     * Returns all labels set on node with id {@code nodeId}.
     * If the node has no labels an empty {@link Iterable} will be returned.
     *
     * @param nodeId
     */
    @Override
    public PrimitiveIntIterator nodeGetLabels(long nodeId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public PrimitiveIntIterator nodeGetPropertyKeys(long nodeId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public PrimitiveIntIterator relationshipGetPropertyKeys(long relationshipId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public PrimitiveIntIterator graphGetPropertyKeys() {
        return null;
    }

    @Override
    public PrimitiveIntIterator nodeGetRelationshipTypes(long nodeId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public boolean nodeHasProperty(long nodeId, int propertyKeyId) throws EntityNotFoundException {
        return false;
    }

    @Override
    public Value nodeGetProperty(long nodeId, int propertyKeyId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public boolean relationshipHasProperty(long relationshipId, int propertyKeyId) throws EntityNotFoundException {
        return false;
    }

    @Override
    public Value relationshipGetProperty(long relationshipId, int propertyKeyId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public boolean graphHasProperty(int propertyKeyId) {
        return false;
    }

    @Override
    public Value graphGetProperty(int propertyKeyId) {
        return null;
    }

    @Override
    public <EXCEPTION extends Exception> void relationshipVisit(long relId, RelationshipVisitor<EXCEPTION> visitor) throws EntityNotFoundException, EXCEPTION {

    }

    @Override
    public long nodesGetCount() {
        return 0;
    }

    @Override
    public long relationshipsGetCount() {
        return 0;
    }

    @Override
    public Cursor<NodeItem> nodeCursorById(long nodeId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public Cursor<RelationshipItem> relationshipCursorById(long relId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public Cursor<PropertyItem> nodeGetProperties(NodeItem node) {
        return null;
    }

    @Override
    public Cursor<PropertyItem> relationshipGetProperties(RelationshipItem relationship) {
        return null;
    }

    /**
     * Returns the index rule for the given LabelSchemaDescriptor.
     *
     * @param descriptor
     */
    @Override
    public IndexDescriptor indexGetForSchema(LabelSchemaDescriptor descriptor) throws SchemaRuleNotFoundException {
        return null;
    }

    /**
     * Get all indexes for a label.
     *
     * @param labelId
     */
    @Override
    public Iterator<IndexDescriptor> indexesGetForLabel(int labelId) {
        return null;
    }

    /**
     * Returns all indexes.
     */
    @Override
    public Iterator<IndexDescriptor> indexesGetAll() {
        return null;
    }

    /**
     * Retrieve the state of an index.
     *
     * @param descriptor
     */
    @Override
    public InternalIndexState indexGetState(IndexDescriptor descriptor) throws IndexNotFoundKernelException {
        return null;
    }

    /**
     * Retrieve provider descriptor for an index.
     *
     * @param descriptor
     */
    @Override
    public SchemaIndexProvider.Descriptor indexGetProviderDescriptor(IndexDescriptor descriptor) throws IndexNotFoundKernelException {
        return null;
    }

    /**
     * Retrieve the population progress of an index.
     *
     * @param descriptor
     */
    @Override
    public PopulationProgress indexGetPopulationProgress(IndexDescriptor descriptor) throws IndexNotFoundKernelException {
        return null;
    }

    /**
     * Get the index size.
     *
     * @param descriptor
     */
    @Override
    public long indexSize(IndexDescriptor descriptor) throws IndexNotFoundKernelException {
        return 0;
    }

    /**
     * Calculate the index unique values percentage (range: {@code 0.0} exclusive to {@code 1.0} inclusive).
     *
     * @param descriptor
     */
    @Override
    public double indexUniqueValuesSelectivity(IndexDescriptor descriptor) throws IndexNotFoundKernelException {
        return 0;
    }

    /**
     * Returns the failure description of a failed index.
     *
     * @param descriptor
     */
    @Override
    public String indexGetFailure(IndexDescriptor descriptor) throws IndexNotFoundKernelException {
        return null;
    }

    /**
     * Get all constraints applicable to label and propertyKey.
     *
     * @param descriptor
     */
    @Override
    public Iterator<ConstraintDescriptor> constraintsGetForSchema(SchemaDescriptor descriptor) {
        return null;
    }

    /**
     * Get all constraints applicable to label.
     *
     * @param labelId
     */
    @Override
    public Iterator<ConstraintDescriptor> constraintsGetForLabel(int labelId) {
        return null;
    }

    /**
     * Get all constraints applicable to relationship type.
     *
     * @param typeId
     */
    @Override
    public Iterator<ConstraintDescriptor> constraintsGetForRelationshipType(int typeId) {
        return null;
    }

    /**
     * Get all constraints.
     */
    @Override
    public Iterator<ConstraintDescriptor> constraintsGetAll() {
        return null;
    }

    /**
     * Get the owning constraint for a constraint index. Returns null if the index does not have an owning constraint.
     *
     * @param index
     */
    @Override
    public Long indexGetOwningUniquenessConstraintId(IndexDescriptor index) {
        return null;
    }

    @Override
    public <K, V> V schemaStateGetOrCreate(K key, Function<K, V> creator) {
        return null;
    }

    @Override
    public <K, V> V schemaStateGet(K key) {
        return null;
    }

    @Override
    public void schemaStateFlush() {

    }

    @Override
    public void acquireExclusive(ResourceType type, long... ids) {

    }

    @Override
    public void acquireShared(ResourceType type, long... ids) {

    }

    @Override
    public void releaseExclusive(ResourceType type, long... ids) {

    }

    @Override
    public void releaseShared(ResourceType type, long... ids) {

    }

    /**
     * @param indexName           name of node index to check for existence.
     * @param customConfiguration if {@code null} the configuration of existing won't be matched, otherwise it will
     *                            be matched and a mismatch will throw {@link IllegalArgumentException}.
     * @return whether or not node explicit index with name {@code indexName} exists.
     * @throws IllegalArgumentException on index existence with provided mismatching {@code customConfiguration}.
     */
    @Override
    public boolean nodeExplicitIndexExists(String indexName, Map<String, String> customConfiguration) {
        return false;
    }

    /**
     * @param indexName           name of relationship index to check for existence.
     * @param customConfiguration if {@code null} the configuration of existing won't be matched, otherwise it will
     *                            be matched and a mismatch will throw {@link IllegalArgumentException}.
     * @return whether or not relationship explicit index with name {@code indexName} exists.
     * @throws IllegalArgumentException on index existence with provided mismatching {@code customConfiguration}.
     */
    @Override
    public boolean relationshipExplicitIndexExists(String indexName, Map<String, String> customConfiguration) {
        return false;
    }

    @Override
    public Map<String, String> nodeExplicitIndexGetConfiguration(String indexName) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public Map<String, String> relationshipExplicitIndexGetConfiguration(String indexName) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public ExplicitIndexHits nodeExplicitIndexGet(String indexName, String key, Object value) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public ExplicitIndexHits nodeExplicitIndexQuery(String indexName, String key, Object queryOrQueryObject) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public ExplicitIndexHits nodeExplicitIndexQuery(String indexName, Object queryOrQueryObject) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    /**
     * @param name
     * @param key
     * @param valueOrNull
     * @param startNode   -1 if ignored.
     * @param endNode     -1 if ignored.
     */
    @Override
    public ExplicitIndexHits relationshipExplicitIndexGet(String name, String key, Object valueOrNull, long startNode, long endNode) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    /**
     * @param indexName
     * @param key
     * @param queryOrQueryObject
     * @param startNode          -1 if ignored.
     * @param endNode            -1 if ignored.
     */
    @Override
    public ExplicitIndexHits relationshipExplicitIndexQuery(String indexName, String key, Object queryOrQueryObject, long startNode, long endNode) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    /**
     * @param indexName
     * @param queryOrQueryObject
     * @param startNode          -1 if ignored.
     * @param endNode            -1 if ignored.
     */
    @Override
    public ExplicitIndexHits relationshipExplicitIndexQuery(String indexName, Object queryOrQueryObject, long startNode, long endNode) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public String[] nodeExplicitIndexesGetAll() {
        return new String[0];
    }

    @Override
    public String[] relationshipExplicitIndexesGetAll() {
        return new String[0];
    }

    /**
     * The number of nodes in the graph, including anything changed in the transaction state.
     * <p>
     * If the label parameter is {@link #ANY_LABEL}, this method returns the total number of nodes in the graph, i.e.
     * {@code MATCH (n) RETURN count(n)}.
     * <p>
     * If the label parameter is set to any other value, this method returns the number of nodes that has that label,
     * i.e. {@code MATCH (n:LBL) RETURN count(n)}.
     *
     * @param labelId the label to get the count for, or {@link #ANY_LABEL} to get the total number of nodes.
     * @return the number of matching nodes in the graph.
     */
    @Override
    public long countsForNode(int labelId) {
        return 0;
    }

    /**
     * The number of nodes in the graph, without taking into account anything in the transaction state.
     * <p>
     * If the label parameter is {@link #ANY_LABEL}, this method returns the total number of nodes in the graph, i.e.
     * {@code MATCH (n) RETURN count(n)}.
     * <p>
     * If the label parameter is set to any other value, this method returns the number of nodes that has that label,
     * i.e. {@code MATCH (n:LBL) RETURN count(n)}.
     *
     * @param labelId the label to get the count for, or {@link #ANY_LABEL} to get the total number of nodes.
     * @return the number of matching nodes in the graph.
     */
    @Override
    public long countsForNodeWithoutTxState(int labelId) {
        return 0;
    }

    /**
     * The number of relationships in the graph, including anything changed in the transaction state.
     * <p>
     * Returns the number of relationships in the graph that matches the specified pattern,
     * {@code (:startLabelId)-[:typeId]->(:endLabelId)}, like so:
     *
     * <table>
     * <thead>
     * <tr><th>{@code startLabelId}</th><th>{@code typeId}</th>                  <th>{@code endLabelId}</th>
     * <td></td>                 <th>Pattern</th>                       <td></td></tr>
     * </thead>
     * <tdata>
     * <tr>
     * <td>{@link #ANY_LABEL}</td>      <td>{@link #ANY_RELATIONSHIP_TYPE}</td>  <td>{@link #ANY_LABEL}</td>
     * <td>{@code MATCH}</td>    <td>{@code ()-[r]->()}</td>            <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@link #ANY_LABEL}</td>      <td>{@code REL}</td>                     <td>{@link #ANY_LABEL}</td>
     * <td>{@code MATCH}</td>    <td>{@code ()-[r:REL]->()}</td>        <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@code LHS}</td>             <td>{@link #ANY_RELATIONSHIP_TYPE}</td>  <td>{@link #ANY_LABEL}</td>
     * <td>{@code MATCH}</td>    <td>{@code (:LHS)-[r]->()}</td>        <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@link #ANY_LABEL}</td>      <td>{@link #ANY_RELATIONSHIP_TYPE}</td>  <td>{@code RHS}</td>
     * <td>{@code MATCH}</td>    <td>{@code ()-[r]->(:RHS)}</td>        <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@code LHS}</td>             <td>{@code REL}</td>                     <td>{@link #ANY_LABEL}</td>
     * <td>{@code MATCH}</td>    <td>{@code (:LHS)-[r:REL]->()}</td>    <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@link #ANY_LABEL}</td>      <td>{@code REL}</td>                     <td>{@code RHS}</td>
     * <td>{@code MATCH}</td>    <td>{@code ()-[r:REL]->(:RHS)}</td>    <td>{@code RETURN count(r)}</td>
     * </tr>
     * </tdata>
     * </table>
     *
     * @param startLabelId the label of the start node of relationships to get the count for, or {@link #ANY_LABEL}.
     * @param typeId       the type of relationships to get a count for, or {@link #ANY_RELATIONSHIP_TYPE}.
     * @param endLabelId   the label of the end node of relationships to get the count for, or {@link #ANY_LABEL}.
     * @return the number of matching relationships in the graph.
     */
    @Override
    public long countsForRelationship(int startLabelId, int typeId, int endLabelId) {
        return 0;
    }

    /**
     * The number of relationships in the graph, without taking into account anything in the transaction state.
     * <p>
     * Returns the number of relationships in the graph that matches the specified pattern,
     * {@code (:startLabelId)-[:typeId]->(:endLabelId)}, like so:
     *
     * <table>
     * <thead>
     * <tr><th>{@code startLabelId}</th><th>{@code typeId}</th>                  <th>{@code endLabelId}</th>
     * <td></td>                 <th>Pattern</th>                       <td></td></tr>
     * </thead>
     * <tdata>
     * <tr>
     * <td>{@link #ANY_LABEL}</td>      <td>{@link #ANY_RELATIONSHIP_TYPE}</td>  <td>{@link #ANY_LABEL}</td>
     * <td>{@code MATCH}</td>    <td>{@code ()-[r]->()}</td>            <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@link #ANY_LABEL}</td>      <td>{@code REL}</td>                     <td>{@link #ANY_LABEL}</td>
     * <td>{@code MATCH}</td>    <td>{@code ()-[r:REL]->()}</td>        <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@code LHS}</td>             <td>{@link #ANY_RELATIONSHIP_TYPE}</td>  <td>{@link #ANY_LABEL}</td>
     * <td>{@code MATCH}</td>    <td>{@code (:LHS)-[r]->()}</td>        <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@link #ANY_LABEL}</td>      <td>{@link #ANY_RELATIONSHIP_TYPE}</td>  <td>{@code RHS}</td>
     * <td>{@code MATCH}</td>    <td>{@code ()-[r]->(:RHS)}</td>        <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@code LHS}</td>             <td>{@code REL}</td>                     <td>{@link #ANY_LABEL}</td>
     * <td>{@code MATCH}</td>    <td>{@code (:LHS)-[r:REL]->()}</td>    <td>{@code RETURN count(r)}</td>
     * </tr>
     * <tr>
     * <td>{@link #ANY_LABEL}</td>      <td>{@code REL}</td>                     <td>{@code RHS}</td>
     * <td>{@code MATCH}</td>    <td>{@code ()-[r:REL]->(:RHS)}</td>    <td>{@code RETURN count(r)}</td>
     * </tr>
     * </tdata>
     * </table>
     *
     * @param startLabelId the label of the start node of relationships to get the count for, or {@link #ANY_LABEL}.
     * @param typeId       the type of relationships to get a count for, or {@link #ANY_RELATIONSHIP_TYPE}.
     * @param endLabelId   the label of the end node of relationships to get the count for, or {@link #ANY_LABEL}.
     * @return the number of matching relationships in the graph.
     */
    @Override
    public long countsForRelationshipWithoutTxState(int startLabelId, int typeId, int endLabelId) {
        return 0;
    }

    @Override
    public Register.DoubleLongRegister indexUpdatesAndSize(IndexDescriptor index, Register.DoubleLongRegister target) throws IndexNotFoundKernelException {
        return null;
    }

    @Override
    public Register.DoubleLongRegister indexSample(IndexDescriptor index, Register.DoubleLongRegister target) throws IndexNotFoundKernelException {
        return null;
    }

    /**
     * Fetch a procedure given its signature.
     *
     * @param name
     */
    @Override
    public ProcedureSignature procedureGet(QualifiedName name) throws ProcedureException {
        return null;
    }

    /**
     * Fetch a function given its signature, or <code>empty</code> if no such function exists
     *
     * @param name
     */
    @Override
    public Optional<UserFunctionSignature> functionGet(QualifiedName name) {
        return Optional.empty();
    }

    /**
     * Fetch an aggregation function given its signature, or <code>empty</code> if no such function exists
     *
     * @param name
     */
    @Override
    public Optional<UserFunctionSignature> aggregationFunctionGet(QualifiedName name) {
        return Optional.empty();
    }

    /**
     * Fetch all registered procedures
     */
    @Override
    public Set<UserFunctionSignature> functionsGetAll() {
        return null;
    }

    /**
     * Fetch all registered procedures
     */
    @Override
    public Set<ProcedureSignature> proceduresGetAll() {
        return null;
    }

    /**
     * Returns a label id for a label name. If the label doesn't exist prior to
     * this call it gets created.
     *
     * @param labelName
     */
    @Override
    public int labelGetOrCreateForName(String labelName) throws IllegalTokenNameException, TooManyLabelsException {
        return 0;
    }

    /**
     * Returns a property key id for a property key. If the key doesn't exist prior to
     * this call it gets created.
     *
     * @param propertyKeyName
     */
    @Override
    public int propertyKeyGetOrCreateForName(String propertyKeyName) throws IllegalTokenNameException {
        return 0;
    }

    @Override
    public int relationshipTypeGetOrCreateForName(String relationshipTypeName) throws IllegalTokenNameException {
        return 0;
    }

    @Override
    public void labelCreateForName(String labelName, int id) throws IllegalTokenNameException, TooManyLabelsException {

    }

    @Override
    public void propertyKeyCreateForName(String propertyKeyName, int id) throws IllegalTokenNameException {

    }

    @Override
    public void relationshipTypeCreateForName(String relationshipTypeName, int id) throws IllegalTokenNameException {

    }

    /**
     * Creates an index, indexing properties with the given {@code propertyKeyId} for nodes with the given
     * {@code labelId}.
     *
     * @param state
     * @param descriptor
     */
    @Override
    public IndexDescriptor indexCreate(KernelStatement state, LabelSchemaDescriptor descriptor) throws AlreadyIndexedException, AlreadyConstrainedException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    /**
     * Drops a {@link IndexDescriptor} from the database
     *
     * @param state
     * @param descriptor
     */
    @Override
    public void indexDrop(KernelStatement state, IndexDescriptor descriptor) throws DropIndexFailureException {

    }

    /**
     * This should not be used, it is exposed to allow an external job to clean up constraint indexes.
     * That external job should become an internal job, at which point this operation should go away.
     *
     * @param state
     * @param descriptor
     */
    @Override
    public void uniqueIndexDrop(KernelStatement state, IndexDescriptor descriptor) throws DropIndexFailureException {

    }

    @Override
    public NodeKeyConstraintDescriptor nodeKeyConstraintCreate(KernelStatement state, LabelSchemaDescriptor descriptor) throws AlreadyConstrainedException, CreateConstraintFailureException, AlreadyIndexedException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    @Override
    public UniquenessConstraintDescriptor uniquePropertyConstraintCreate(KernelStatement state, LabelSchemaDescriptor descriptor) throws AlreadyConstrainedException, CreateConstraintFailureException, AlreadyIndexedException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    @Override
    public NodeExistenceConstraintDescriptor nodePropertyExistenceConstraintCreate(KernelStatement state, LabelSchemaDescriptor descriptor) throws AlreadyConstrainedException, CreateConstraintFailureException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    @Override
    public RelExistenceConstraintDescriptor relationshipPropertyExistenceConstraintCreate(KernelStatement state, RelationTypeSchemaDescriptor descriptor) throws AlreadyConstrainedException, CreateConstraintFailureException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    @Override
    public void constraintDrop(KernelStatement state, ConstraintDescriptor constraint) throws DropConstraintFailureException {

    }
}
