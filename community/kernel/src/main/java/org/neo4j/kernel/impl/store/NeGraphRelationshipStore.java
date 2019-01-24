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
package org.neo4j.kernel.impl.store;

import org.cam.storage.levelgraph.storage.NeRelationshipStore;
import org.cam.storage.levelgraph.storage.ondiskstorage.FileStorageLayerInterface;
import org.neo4j.helpers.collection.Visitor;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.PagedFile;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.store.format.RecordFormats;
import org.neo4j.kernel.impl.store.format.standard.RelationshipRecordFormat;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdRange;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.Logger;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.util.*;

import static org.neo4j.io.pagecache.PagedFile.PF_SHARED_WRITE_LOCK;


public class NeGraphRelationshipStore extends RelationshipStore {
    FileStorageLayerInterface fileStorageLayerInterface;
    NeRelationshipStore store;
    private HashMap<Long, RelationshipRecord> recordCache;
    private Deque<Long> updatedSinceLastFlush;

    public NeGraphRelationshipStore(
            File fileName,
            Config configuration,
            IdGeneratorFactory idGeneratorFactory,
            PageCache pageCache,
            LogProvider logProvider,
            RecordFormats recordFormats,
            OpenOption... openOptions) {
        super(fileName, configuration, idGeneratorFactory,
                pageCache, logProvider, recordFormats,
                openOptions);
        store = new NeRelationshipStore(fileName.getPath() + "nerelation", pageCache);
        recordCache = new HashMap<>();
        updatedSinceLastFlush = new ArrayDeque<>();
        fileStorageLayerInterface = new FileStorageLayerInterface(fileName.getPath() + "negraph");
    }

    /**
     * Acquires a {@link PageCursor} from the {@link PagedFile store file} and reads the requested record
     * in the correct page and offset.
     *
     * @param id     the record id.
     * @param record the record instance to load the data into.
     * @param mode   how strict to be when loading, f.ex {@link RecordLoad#FORCE} will always read what's there
     *               and load into the record, whereas {@link RecordLoad#NORMAL} will throw {@link InvalidRecordException}
     */
    @Override
    public RelationshipRecord getRecord(long id, RelationshipRecord record, RecordLoad mode) {
        if (recordCache.containsKey(id)) {
            return recordCache.get(id).copy(record);
        }
        store.getRecord(id, record, mode);
        RelationshipRecord duplicate=record.clone();
//            fileStorageLayerInterface.returnRecord(id, record);
        super.getRecord(id, record, mode);
        if(!duplicate.equals(record)){
            duplicate.equals(record);
        }
        return record;
    }

    @Override
    void readIntoRecord(long id, RelationshipRecord record, RecordLoad mode, PageCursor cursor) throws IOException {
        record.setId(id);
        if (recordCache.containsKey(id)) {
            recordCache.get(id).copy(record);
            return;
        }
        store.readIntoRecord(id, record, mode);
        RelationshipRecord duplicate=record.clone();
        super.readIntoRecord(id, record, mode, cursor);
        if(!duplicate.equals(record)){
            duplicate.equals(record);
        }
    }

    @Override
    public void updateRecord(RelationshipRecord record) {
        long id = record.getId();
        if (!record.inUse()) {
            recordCache.remove(id);
            long pageId = pageIdForRecord(id);
            freeId(record.getId());
            try (PageCursor cursor = storeFile.io(pageId, PF_SHARED_WRITE_LOCK)) {
                ((RelationshipRecordFormat) recordFormat).markUnused(cursor);
            } catch (IOException e) {
                throw new UnderlyingStorageException(e);
            }
        }
        if ((!record.inUse() || !record.requiresSecondaryUnit()) && record.hasSecondaryUnitId()) {
            // If record was just now deleted, or if the record used a secondary unit, but not anymore
            // then free the id of that secondary unit.
            freeId(record.getSecondaryUnitId());
            return;
        }
        store.updateRecord(record);
        RelationshipRecord duplicate=record.clone();
        super.updateRecord(record);
        if(!duplicate.equals(record)){
            duplicate.equals(record);
        }
    }

    /**************************************************************************************************************/
    @Override
    void initialise(boolean createIfNotExists) {
        store.initialise(createIfNotExists);
        super.initialise(createIfNotExists);
    }

    /**
     * Returns the type and version that identifies this store.
     *
     * @return This store's implementation type and version identifier
     */
    @Override
    public String getTypeDescriptor() {
        return super.getTypeDescriptor();
    }

    /**
     * This method is called by constructors. Checks the header record and loads the store.
     * <p>
     * Note: This method will map the file with the page cache. The store file must not
     * be accessed directly until it has been unmapped - the store file must only be
     * accessed through the page cache.
     *
     * @param createIfNotExists If true, creates and initialises the store file if it does not exist already. If false,
     *                          this method will instead throw an exception in that situation.
     */
    @Override
    protected void checkAndLoadStorage(boolean createIfNotExists) {
        super.checkAndLoadStorage(createIfNotExists);
    }

    @Override
    protected void initialiseNewStoreFile(PagedFile file) throws IOException {
        super.initialiseNewStoreFile(file);
    }

    @Override
    protected long pageIdForRecord(long id) {
        return super.pageIdForRecord(id);
    }

    @Override
    protected int offsetForId(long id) {
        return super.offsetForId(id);
    }

    @Override
    public int getRecordsPerPage() {
        return super.getRecordsPerPage();
    }

    @Override
    public byte[] getRawRecordData(long id) throws IOException {
        return super.getRawRecordData(id);
    }

    @Override
    public boolean isInUse(long id) {
        return super.isInUse(id);
    }

    @Override
    protected boolean isOnlyFastIdGeneratorRebuildEnabled(Config config) {
        return super.isOnlyFastIdGeneratorRebuildEnabled(config);
    }

    /**
     * Marks this store as "not ok".
     *
     * @param cause
     */
    @Override
    void setStoreNotOk(Throwable cause) {
        super.setStoreNotOk(cause);
    }

    /**
     * If store is "not ok" <CODE>false</CODE> is returned.
     *
     * @return True if this store is ok
     */
    @Override
    boolean getStoreOk() {
        return super.getStoreOk();
    }

    /**
     * Throws cause of not being OK if {@link #getStoreOk()} returns {@code false}.
     */
    @Override
    void checkStoreOk() {
        super.checkStoreOk();
    }

    /**
     * @return The next free id
     */
    @Override
    public long nextId() {
        return super.nextId();
    }

    @Override
    public IdRange nextIdBatch(int size) {
        return super.nextIdBatch(size);
    }

    /**
     * @param id The id to free
     */
    @Override
    public void freeId(long id) {
        super.freeId(id);
    }

    /**
     * Return the highest id in use. If this store is not OK yet, the high id is calculated from the highest
     * in use record on the store, using {@link #scanForHighId()}.
     *
     * @return The high id, i.e. highest id in use + 1.
     */
    @Override
    public long getHighId() {
        return super.getHighId();
    }

    /**
     * Sets the high id, i.e. highest id in use + 1 (use this when rebuilding id generator).
     *
     * @param highId The high id to set.
     */
    @Override
    public void setHighId(long highId) {
        super.setHighId(highId);
    }

    /**
     * If store is not ok a call to this method will rebuild the {@link
     * <p>
     * WARNING: this method must NOT be called if recovery is required, but hasn't performed.
     * To remove all negations from the above statement: Only call this method if store is in need of
     * recovery and recovery has been performed.
     */
    @Override
    void makeStoreOk() {
        super.makeStoreOk();
    }

    /**
     * Returns the name of this store.
     *
     * @return The name of this store
     */
    @Override
    public File getStorageFileName() {
        return super.getStorageFileName();
    }

    /**
     * <p>
     * Note: This method may be called both while the store has the store file mapped in the
     * page cache, and while the store file is not mapped. Implementers must therefore
     * map their own temporary PagedFile for the store file, and do their file IO through that,
     * if they need to access the data in the store file.
     */
    @Override
    void openIdGenerator() {
        super.openIdGenerator();
    }

    /**
     * Starts from the end of the file and scans backwards to find the highest in use record.
     * Can be used even if {@link #makeStoreOk()} hasn't been called. Basically this method should be used
     * over {@link #getHighestPossibleIdInUse()} and {@link #getHighId()} in cases where a store has been opened
     * but is in a scenario where recovery isn't possible, like some tooling or migration.
     *
     * @return the id of the highest in use record + 1, i.e. highId.
     */
    @Override
    protected long scanForHighId() {
        return super.scanForHighId();
    }

    @Override
    protected int determineRecordSize() {
        return super.determineRecordSize();
    }

    @Override
    public int getRecordDataSize() {
        return super.getRecordDataSize();
    }

    @Override
    protected boolean isRecordReserved(PageCursor cursor) {
        return super.isRecordReserved(cursor);
    }

    /**
     *
     */
    @Override
    void closeIdGenerator() {
        super.closeIdGenerator();
    }

    @Override
    public void flush() {
        super.flush();
    }

    /**
     * Checks if this store is closed and throws exception if it is.
     *
     * @throws IllegalStateException if the store is closed
     */
    @Override
    void assertNotClosed() {
        super.assertNotClosed();
    }

    /**
     * Closes this store. This will cause all buffers and channels to be closed.
     * Requesting an operation from after this method has been invoked is
     * illegal and an exception will be thrown.
     * <p>
     * giving the implementing store way to do anything that it needs to do
     * before the pagedFile is closed.
     */
    @Override
    public void close() {
        super.close();
    }

    /**
     * @return The highest possible id in use, -1 if no id in use.
     */
    @Override
    public long getHighestPossibleIdInUse() {
        return super.getHighestPossibleIdInUse();
    }

    /**
     * Sets the highest id in use. After this call highId will be this given id + 1.
     *
     * @param highId The highest id in use to set.
     */
    @Override
    public void setHighestPossibleIdInUse(long highId) {
        super.setHighestPossibleIdInUse(highId);
    }

    /**
     * @return The total number of ids in use.
     */
    @Override
    public long getNumberOfIdsInUse() {
        return super.getNumberOfIdsInUse();
    }

    /**
     * @return the number of records at the beginning of the store file that are reserved for other things
     * than actual records. Stuff like permanent configuration data.
     */
    @Override
    public int getNumberOfReservedLowIds() {
        return super.getNumberOfReservedLowIds();
    }

    @Override
    public IdType getIdType() {
        return super.getIdType();
    }

    @Override
    void logVersions(Logger logger) {
        super.logVersions(logger);
    }

    @Override
    void logIdUsage(Logger logger) {
        super.logIdUsage(logger);
    }

    /**
     * Visits this store, and any other store managed by this store.
     * TODO this could, and probably should, replace all override-and-do-the-same-thing-to-all-my-managed-stores
     * methods like:
     * {@link #makeStoreOk()},
     * {@link #close()} (where that method could be deleted all together and do a visit in {@link #close()}),
     * {@link #logIdUsage(Logger)},
     * {@link #logVersions(Logger)}
     * For a good samaritan to pick up later.
     *
     * @param visitor
     */
    @Override
    void visitStore(Visitor<CommonAbstractStore<RelationshipRecord, NoStoreHeader>, RuntimeException> visitor) {
        super.visitStore(visitor);
    }

    @Override
    public long getNextRecordReference(RelationshipRecord record) {
        return super.getNextRecordReference(record);
    }

    @Override
    public RelationshipRecord newRecord() {
        return super.newRecord();
    }


    @Override
    public void prepareForCommit(RelationshipRecord record) {
        super.prepareForCommit(record);
    }

    @Override
    public <EXCEPTION extends Exception> void scanAllRecords(Visitor<RelationshipRecord, EXCEPTION> visitor) throws EXCEPTION {
        super.scanAllRecords(visitor);
    }

    @Override
    public Collection<RelationshipRecord> getRecords(long firstId, RecordLoad mode) {
        return super.getRecords(firstId, mode);
    }

    @Override
    public RecordCursor<RelationshipRecord> newRecordCursor(RelationshipRecord record) {
        return super.newRecordCursor(record);
    }

    @Override
    public void ensureHeavy(RelationshipRecord record) {
        super.ensureHeavy(record);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int getStoreHeaderInt() {
        return super.getStoreHeaderInt();
    }
}
