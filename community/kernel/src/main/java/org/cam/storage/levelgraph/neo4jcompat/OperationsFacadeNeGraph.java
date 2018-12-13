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

import org.cam.storage.levelgraph.transaction.TransactionalStorage;
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
import org.neo4j.kernel.impl.api.KernelStatement;
import org.neo4j.kernel.impl.api.RelationshipVisitor;
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

public class OperationsFacadeNeGraph implements ReadOperations, DataWriteOperations, TokenWriteOperations, SchemaWriteOperations,
        QueryRegistryOperations, ProcedureCallOperations, ExecutionStatisticsOperations {

    TransactionalStorage storage;

    @Override
    public long nodeCreate() {
        return storage.nodeCreate();
    }

    @Override
    public void nodeDelete(long l) throws EntityNotFoundException, InvalidTransactionTypeKernelException, AutoIndexingKernelException {
        storage.nodeDelete(l);
    }

    @Override
    public int nodeDetachDelete(long l) throws KernelException {
        return storage.nodeDetachDelete(l);
    }

    @Override
    public long relationshipCreate(int i, long l, long l1) throws RelationshipTypeIdNotFoundKernelException, EntityNotFoundException {
        return storage.relationshipCreate(i, l, l1);
    }

    @Override
    public void relationshipDelete(long l) throws EntityNotFoundException, InvalidTransactionTypeKernelException, AutoIndexingKernelException {
        storage.relationshipDelete(l);
    }

    @Override
    public boolean nodeAddLabel(long l, int i) throws EntityNotFoundException, ConstraintValidationException {
        return storage.nodeAddLabel(l,i);
    }

    @Override
    public boolean nodeRemoveLabel(long l, int i) throws EntityNotFoundException {
        return storage.nodeRemoveLabel(l,i);
    }

    @Override
    public Value nodeSetProperty(long l, int i, Value value) throws EntityNotFoundException, AutoIndexingKernelException, InvalidTransactionTypeKernelException, ConstraintValidationException {
        return storage.nodeSetProperty(l,i,value);
    }

    @Override
    public Value relationshipSetProperty(long l, int i, Value value) throws EntityNotFoundException, AutoIndexingKernelException, InvalidTransactionTypeKernelException {
        return storage.relationshipSetProperty(l,i,value);
    }

    @Override
    public Value graphSetProperty(int i, Value value) {
        return null;
    }

    @Override
    public Value nodeRemoveProperty(long l, int i) throws EntityNotFoundException, AutoIndexingKernelException, InvalidTransactionTypeKernelException {
        return storage.nodeRemoveProperty(l,i);
    }

    @Override
    public Value relationshipRemoveProperty(long l, int i) throws EntityNotFoundException, AutoIndexingKernelException, InvalidTransactionTypeKernelException {
        return storage.relationshipRemoveProperty(l,i);
    }

    @Override
    public Value graphRemoveProperty(int i) {
        return null;
    }

    @Override
    public void nodeExplicitIndexCreateLazily(String s, Map<String, String> map) {

    }

    @Override
    public void nodeExplicitIndexCreate(String s, Map<String, String> map) {

    }

    @Override
    public void relationshipExplicitIndexCreateLazily(String s, Map<String, String> map) {

    }

    @Override
    public void relationshipExplicitIndexCreate(String s, Map<String, String> map) {

    }

    @Override
    public String nodeExplicitIndexSetConfiguration(String s, String s1, String s2) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public String relationshipExplicitIndexSetConfiguration(String s, String s1, String s2) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public String nodeExplicitIndexRemoveConfiguration(String s, String s1) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public String relationshipExplicitIndexRemoveConfiguration(String s, String s1) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public void nodeAddToExplicitIndex(String s, long l, String s1, Object o) throws EntityNotFoundException, ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void nodeRemoveFromExplicitIndex(String s, long l, String s1, Object o) throws ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void nodeRemoveFromExplicitIndex(String s, long l, String s1) throws ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void nodeRemoveFromExplicitIndex(String s, long l) throws ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void relationshipAddToExplicitIndex(String s, long l, String s1, Object o) throws EntityNotFoundException, ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void relationshipRemoveFromExplicitIndex(String s, long l, String s1, Object o) throws ExplicitIndexNotFoundKernelException, EntityNotFoundException {

    }

    @Override
    public void relationshipRemoveFromExplicitIndex(String s, long l, String s1) throws ExplicitIndexNotFoundKernelException, EntityNotFoundException {

    }

    @Override
    public void relationshipRemoveFromExplicitIndex(String s, long l) throws ExplicitIndexNotFoundKernelException, EntityNotFoundException {

    }

    @Override
    public void nodeExplicitIndexDrop(String s) throws ExplicitIndexNotFoundKernelException {

    }

    @Override
    public void relationshipExplicitIndexDrop(String s) throws ExplicitIndexNotFoundKernelException {

    }

    @Override
    public PageCursorTracer getPageCursorTracer() {
        return null;
    }

    @Override
    public RawIterator<Object[], ProcedureException> procedureCallRead(QualifiedName qualifiedName, Object[] objects) throws ProcedureException {
        return null;
    }

    @Override
    public RawIterator<Object[], ProcedureException> procedureCallReadOverride(QualifiedName qualifiedName, Object[] objects) throws ProcedureException {
        return null;
    }

    @Override
    public RawIterator<Object[], ProcedureException> procedureCallWrite(QualifiedName qualifiedName, Object[] objects) throws ProcedureException {
        return null;
    }

    @Override
    public RawIterator<Object[], ProcedureException> procedureCallWriteOverride(QualifiedName qualifiedName, Object[] objects) throws ProcedureException {
        return null;
    }

    @Override
    public RawIterator<Object[], ProcedureException> procedureCallSchema(QualifiedName qualifiedName, Object[] objects) throws ProcedureException {
        return null;
    }

    @Override
    public RawIterator<Object[], ProcedureException> procedureCallSchemaOverride(QualifiedName qualifiedName, Object[] objects) throws ProcedureException {
        return null;
    }

    @Override
    public Object functionCall(QualifiedName qualifiedName, Object[] objects) throws ProcedureException {
        return null;
    }

    @Override
    public Object functionCallOverride(QualifiedName qualifiedName, Object[] objects) throws ProcedureException {
        return null;
    }

    @Override
    public CallableUserAggregationFunction.Aggregator aggregationFunction(QualifiedName qualifiedName) throws ProcedureException {
        return null;
    }

    @Override
    public CallableUserAggregationFunction.Aggregator aggregationFunctionOverride(QualifiedName qualifiedName) throws ProcedureException {
        return null;
    }

    @Override
    public Map<String, Object> getMetaData() {
        return null;
    }

    @Override
    public void setMetaData(Map<String, Object> map) {

    }

    @Override
    public Stream<ExecutingQuery> executingQueries() {
        return null;
    }

    @Override
    public ExecutingQuery startQueryExecution(ClientConnectionInfo clientConnectionInfo, String s, MapValue mapValue) {
        return null;
    }

    @Override
    public void registerExecutingQuery(ExecutingQuery executingQuery) {

    }

    @Override
    public void unregisterExecutingQuery(ExecutingQuery executingQuery) {

    }

    @Override
    public int labelGetForName(String s) {
        return storage.labelGetForName(s);
    }

    @Override
    public String labelGetName(int i) throws LabelNotFoundKernelException {
        return storage.labelGetName(i);
    }

    @Override
    public Iterator<Token> labelsGetAllTokens() {
        return null;
    }

    @Override
    public int propertyKeyGetForName(String s) {
        return storage.propertyKeyGetForName(s);
    }

    @Override
    public String propertyKeyGetName(int i) throws PropertyKeyIdNotFoundKernelException {
        return storage.propertyKeyGetName(i);
    }

    @Override
    public Iterator<Token> propertyKeyGetAllTokens() {
        return null;
    }

    @Override
    public int relationshipTypeGetForName(String s) {
        return storage.relationshipTypeGetForName(s);
    }

    @Override
    public String relationshipTypeGetName(int i) throws RelationshipTypeIdNotFoundKernelException {
        return storage.relationshipTypeGetName(i);
    }

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

    @Override
    public PrimitiveLongIterator nodesGetForLabel(int i) {
        return null;
    }

    @Override
    public PrimitiveLongIterator indexQuery(IndexDescriptor indexDescriptor, IndexQuery... indexQueries) throws IndexNotFoundKernelException, IndexNotApplicableKernelException {
        return null;
    }

    @Override
    public PrimitiveLongIterator nodesGetAll() {
        return null;
    }

    @Override
    public PrimitiveLongIterator relationshipsGetAll() {
        return null;
    }

    @Override
    public RelationshipIterator nodeGetRelationships(long l, Direction direction, int[] ints) throws EntityNotFoundException {
        return null;
    }

    @Override
    public RelationshipIterator nodeGetRelationships(long l, Direction direction) throws EntityNotFoundException {
        return null;
    }

    @Override
    public long nodeGetFromUniqueIndexSeek(IndexDescriptor indexDescriptor, IndexQuery.ExactPredicate... exactPredicates) throws IndexNotFoundKernelException, IndexBrokenKernelException, IndexNotApplicableKernelException {
        return 0;
    }

    @Override
    public long nodesCountIndexed(IndexDescriptor indexDescriptor, long l, Value value) throws IndexNotFoundKernelException, IndexBrokenKernelException {
        return 0;
    }

    @Override
    public boolean nodeExists(long l) {
        return false;
    }

    @Override
    public boolean nodeHasLabel(long l, int i) throws EntityNotFoundException {
        return false;
    }

    @Override
    public int nodeGetDegree(long l, Direction direction, int i) throws EntityNotFoundException {
        return 0;
    }

    @Override
    public int nodeGetDegree(long l, Direction direction) throws EntityNotFoundException {
        return 0;
    }

    @Override
    public boolean nodeIsDense(long l) throws EntityNotFoundException {
        return false;
    }

    @Override
    public PrimitiveIntIterator nodeGetLabels(long l) throws EntityNotFoundException {
        return null;
    }

    @Override
    public PrimitiveIntIterator nodeGetPropertyKeys(long l) throws EntityNotFoundException {
        return null;
    }

    @Override
    public PrimitiveIntIterator relationshipGetPropertyKeys(long l) throws EntityNotFoundException {
        return null;
    }

    @Override
    public PrimitiveIntIterator graphGetPropertyKeys() {
        return null;
    }

    @Override
    public PrimitiveIntIterator nodeGetRelationshipTypes(long l) throws EntityNotFoundException {
        return null;
    }

    @Override
    public boolean nodeHasProperty(long l, int i) throws EntityNotFoundException {
        return false;
    }

    @Override
    public Value nodeGetProperty(long l, int i) throws EntityNotFoundException {
        return null;
    }

    @Override
    public boolean relationshipHasProperty(long l, int i) throws EntityNotFoundException {
        return false;
    }

    @Override
    public Value relationshipGetProperty(long l, int i) throws EntityNotFoundException {
        return null;
    }

    @Override
    public boolean graphHasProperty(int i) {
        return false;
    }

    @Override
    public Value graphGetProperty(int i) {
        return null;
    }

    @Override
    public <EXCEPTION extends Exception> void relationshipVisit(long l, RelationshipVisitor<EXCEPTION> relationshipVisitor) throws EntityNotFoundException, EXCEPTION {

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
    public Cursor<NodeItem> nodeCursorById(long l) throws EntityNotFoundException {
        return null;
    }

    @Override
    public Cursor<RelationshipItem> relationshipCursorById(long l) throws EntityNotFoundException {
        return null;
    }

    @Override
    public Cursor<PropertyItem> nodeGetProperties(NodeItem nodeItem) {
        return null;
    }

    @Override
    public Cursor<PropertyItem> relationshipGetProperties(RelationshipItem relationshipItem) {
        return null;
    }

    @Override
    public IndexDescriptor indexGetForSchema(LabelSchemaDescriptor labelSchemaDescriptor) throws SchemaRuleNotFoundException {
        return null;
    }

    @Override
    public Iterator<IndexDescriptor> indexesGetForLabel(int i) {
        return null;
    }

    @Override
    public Iterator<IndexDescriptor> indexesGetAll() {
        return null;
    }

    @Override
    public InternalIndexState indexGetState(IndexDescriptor indexDescriptor) throws IndexNotFoundKernelException {
        return null;
    }

    @Override
    public SchemaIndexProvider.Descriptor indexGetProviderDescriptor(IndexDescriptor indexDescriptor) throws IndexNotFoundKernelException {
        return null;
    }

    @Override
    public PopulationProgress indexGetPopulationProgress(IndexDescriptor indexDescriptor) throws IndexNotFoundKernelException {
        return null;
    }

    @Override
    public long indexSize(IndexDescriptor indexDescriptor) throws IndexNotFoundKernelException {
        return 0;
    }

    @Override
    public double indexUniqueValuesSelectivity(IndexDescriptor indexDescriptor) throws IndexNotFoundKernelException {
        return 0;
    }

    @Override
    public String indexGetFailure(IndexDescriptor indexDescriptor) throws IndexNotFoundKernelException {
        return null;
    }

    @Override
    public Iterator<ConstraintDescriptor> constraintsGetForSchema(SchemaDescriptor schemaDescriptor) {
        return null;
    }

    @Override
    public Iterator<ConstraintDescriptor> constraintsGetForLabel(int i) {
        return null;
    }

    @Override
    public Iterator<ConstraintDescriptor> constraintsGetForRelationshipType(int i) {
        return null;
    }

    @Override
    public Iterator<ConstraintDescriptor> constraintsGetAll() {
        return null;
    }

    @Override
    public Long indexGetOwningUniquenessConstraintId(IndexDescriptor indexDescriptor) {
        return null;
    }

    @Override
    public <K, V> V schemaStateGetOrCreate(K k, Function<K, V> function) {
        return null;
    }

    @Override
    public <K, V> V schemaStateGet(K k) {
        return null;
    }

    @Override
    public void schemaStateFlush() {

    }

    @Override
    public void acquireExclusive(ResourceType resourceType, long... longs) {

    }

    @Override
    public void acquireShared(ResourceType resourceType, long... longs) {

    }

    @Override
    public void releaseExclusive(ResourceType resourceType, long... longs) {

    }

    @Override
    public void releaseShared(ResourceType resourceType, long... longs) {

    }

    @Override
    public boolean nodeExplicitIndexExists(String s, Map<String, String> map) {
        return false;
    }

    @Override
    public boolean relationshipExplicitIndexExists(String s, Map<String, String> map) {
        return false;
    }

    @Override
    public Map<String, String> nodeExplicitIndexGetConfiguration(String s) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public Map<String, String> relationshipExplicitIndexGetConfiguration(String s) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public ExplicitIndexHits nodeExplicitIndexGet(String s, String s1, Object o) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public ExplicitIndexHits nodeExplicitIndexQuery(String s, String s1, Object o) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public ExplicitIndexHits nodeExplicitIndexQuery(String s, Object o) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public ExplicitIndexHits relationshipExplicitIndexGet(String s, String s1, Object o, long l, long l1) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public ExplicitIndexHits relationshipExplicitIndexQuery(String s, String s1, Object o, long l, long l1) throws ExplicitIndexNotFoundKernelException {
        return null;
    }

    @Override
    public ExplicitIndexHits relationshipExplicitIndexQuery(String s, Object o, long l, long l1) throws ExplicitIndexNotFoundKernelException {
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

    @Override
    public long countsForNode(int i) {
        return 0;
    }

    @Override
    public long countsForNodeWithoutTxState(int i) {
        return 0;
    }

    @Override
    public long countsForRelationship(int i, int i1, int i2) {
        return 0;
    }

    @Override
    public long countsForRelationshipWithoutTxState(int i, int i1, int i2) {
        return 0;
    }

    @Override
    public Register.DoubleLongRegister indexUpdatesAndSize(IndexDescriptor indexDescriptor, Register.DoubleLongRegister doubleLongRegister) throws IndexNotFoundKernelException {
        return null;
    }

    @Override
    public Register.DoubleLongRegister indexSample(IndexDescriptor indexDescriptor, Register.DoubleLongRegister doubleLongRegister) throws IndexNotFoundKernelException {
        return null;
    }

    @Override
    public ProcedureSignature procedureGet(QualifiedName qualifiedName) throws ProcedureException {
        return null;
    }

    @Override
    public Optional<UserFunctionSignature> functionGet(QualifiedName qualifiedName) {
        return Optional.empty();
    }

    @Override
    public Optional<UserFunctionSignature> aggregationFunctionGet(QualifiedName qualifiedName) {
        return Optional.empty();
    }

    @Override
    public Set<UserFunctionSignature> functionsGetAll() {
        return null;
    }

    @Override
    public Set<ProcedureSignature> proceduresGetAll() {
        return null;
    }

    @Override
    public int labelGetOrCreateForName(String s) throws IllegalTokenNameException, TooManyLabelsException {
        return 0;
    }

    @Override
    public int propertyKeyGetOrCreateForName(String s) throws IllegalTokenNameException {
        return 0;
    }

    @Override
    public int relationshipTypeGetOrCreateForName(String s) throws IllegalTokenNameException {
        return 0;
    }

    @Override
    public void labelCreateForName(String s, int i) throws IllegalTokenNameException, TooManyLabelsException {

    }

    @Override
    public void propertyKeyCreateForName(String s, int i) throws IllegalTokenNameException {

    }

    @Override
    public void relationshipTypeCreateForName(String s, int i) throws IllegalTokenNameException {

    }

    @Override
    public IndexDescriptor indexCreate(KernelStatement kernelStatement, LabelSchemaDescriptor labelSchemaDescriptor) throws AlreadyIndexedException, AlreadyConstrainedException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    @Override
    public void indexDrop(KernelStatement kernelStatement, IndexDescriptor indexDescriptor) throws DropIndexFailureException {

    }

    @Override
    public void uniqueIndexDrop(KernelStatement kernelStatement, IndexDescriptor indexDescriptor) throws DropIndexFailureException {

    }

    @Override
    public NodeKeyConstraintDescriptor nodeKeyConstraintCreate(KernelStatement kernelStatement, LabelSchemaDescriptor labelSchemaDescriptor) throws AlreadyConstrainedException, CreateConstraintFailureException, AlreadyIndexedException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    @Override
    public UniquenessConstraintDescriptor uniquePropertyConstraintCreate(KernelStatement kernelStatement, LabelSchemaDescriptor labelSchemaDescriptor) throws AlreadyConstrainedException, CreateConstraintFailureException, AlreadyIndexedException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    @Override
    public NodeExistenceConstraintDescriptor nodePropertyExistenceConstraintCreate(KernelStatement kernelStatement, LabelSchemaDescriptor labelSchemaDescriptor) throws AlreadyConstrainedException, CreateConstraintFailureException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    @Override
    public RelExistenceConstraintDescriptor relationshipPropertyExistenceConstraintCreate(KernelStatement kernelStatement, RelationTypeSchemaDescriptor relationTypeSchemaDescriptor) throws AlreadyConstrainedException, CreateConstraintFailureException, RepeatedPropertyInCompositeSchemaException {
        return null;
    }

    @Override
    public void constraintDrop(KernelStatement kernelStatement, ConstraintDescriptor constraintDescriptor) throws DropConstraintFailureException {

    }
}
