package org.cam.storage.levelgraph.storage;

import org.neo4j.graphdb.Relationship;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;

import java.io.File;

/**
 *
 * Handles a store with the following format:
 * File edges, File metadata
 * Edges: 128 bits/16 bytes
 * No edgeid separately provided as the edgeid maps directly to the storage.
 *
 */

public class EdgeStore {

    File dataStore, metdataStore;

    RelationshipRecord getRecord(long id, RelationshipRecord record, RecordLoad mode){
        return null;

    }
    void readIntoRecord(long id, RelationshipRecord record, RecordLoad mode, PageCursor cursor){

    }

    void updateRecord(RelationshipRecord record){

    }
}
