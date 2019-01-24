package org.cam.storage.levelgraph.storage;

import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.PagedFile;
import org.neo4j.kernel.impl.store.UnderlyingStorageException;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.neo4j.io.pagecache.PageCacheOpenOptions.ANY_PAGE_SIZE;

/**
 * Handles a store with the following format:
 * File edges, File metadata
 * Edges: 48 bits/6 bytes.
 * No edgeid separately provided as the edgeid maps directly to the storage.
 */

public class NeRelationshipStore {

    public class StorageType {
        int recordSize;
        int pageSize;
        int header;
        PagedFile pagedFile;
        PageCursor readCursor, writeCursor;

        public StorageType(int recordSize, int pageSize) {
            this.recordSize = recordSize;
            this.pageSize = pageSize;
            header = 0;
        }

        public StorageType(int recordSize, int pageSize, int header) {
            this.recordSize = recordSize;
            this.pageSize = pageSize;
            this.header = header;
        }

        public void initialise(PagedFile pagedFile) throws IOException {
            this.pagedFile = pagedFile;
            writeCursor = pagedFile.io(0, PagedFile.PF_SHARED_WRITE_LOCK);
            readCursor = pagedFile.io(0, PagedFile.PF_SHARED_READ_LOCK);
        }

        public PageCursor getWritePageCursor() {
            return writeCursor;
        }

        public PageCursor getReadPageCursor() {
            return readCursor;
        }

        public PageCursor moveCursorToBlock(long block, PageCursor cursor) throws IOException {
            long pageId = pageIdFromDataId(block);
            int blockOffset = (int) ((block * recordSize) % pageSize);
            cursor.next(pageId);
            cursor.setOffset(blockOffset);
            return cursor;
        }

        private void clearPage(PageCursor cursor, long pageId, int offsetBegin, int offsetEnd) throws IOException {
            cursor.next(pageId);
            for (int currOffset = offsetBegin; currOffset < offsetEnd; currOffset++) {
                cursor.putByte(currOffset, (byte) 0xff);
            }
        }

        private long pageIdFromDataId(long index) {
            return ((index * recordSize) + header) / pageSize;
        }

        int pageOffsetFromId(long index) {
            return (int) (((index * recordSize) + header) % pageSize);
        }

        private void cleanRange(long index1, long index2) {
            long pageId1 = pageIdFromDataId(index1), pageId2 = pageIdFromDataId(index2);
            int start = pageOffsetFromId(index1);
            int end = pageOffsetFromId(index2);
            try (PageCursor cursor = getWritePageCursor()) {
                if (pageId1 == pageId2) {
                    clearPage(cursor, pageId1, start, end);
                }
                clearPage(cursor, pageId1, start, pageSize);
                pageId1++;
                while (pageId1 < pageId2) {
                    clearPage(cursor, pageId1, 0, pageSize);
                    pageId1++;
                }
                clearPage(cursor, pageId2, 0, end);
            } catch (IOException e) {
                throw new UnderlyingStorageException(e);
            }
        }
    }

    private static final int recordStorePageSize = 4092, skipListPageSize = 4047, nodeHeader = 25, alignedPageSize = 4096;
    private static final int recordSize = 6, recordsPerPage = recordStorePageSize / recordSize;
   /*
   Record: 48 bits. with 4 bits of flag and 44 bits of node id and node offset data.
   6 bits of offset and 16 bits of id for each node.
    */

    /*
    Metadata format
    8+1+8+8+32=25+32=57
    NodeId, c                tail = cursor.getLong();
ount, prevblock, nextblock, 4* nodeIds
     */


    /*
    recordStore: The storage system for the data.
    nodeSkipListStore: metadata for each edge block.
    nodeSkipTail: The latest used node block.
    offsetWriter: The offset for a batch of nodes.
     */

    private StorageType recordWrapper, skipListWrapper, skipTailWrapper, offsetWrapper;
    private ArrayList<Long> offsets;
    private HashMap<Long, OffsetData> nodeOffset;
    private long currentOffset, highestNodeId, highestEdgeId;
    private PageCache pageCache;
    private String filename;

    public NeRelationshipStore(String filename, PageCache pageCache) {
        this.pageCache = pageCache;
        this.filename = filename;
        nodeOffset = new HashMap<>();
        currentOffset = 0;
        offsets = new ArrayList<>();
        recordWrapper = new StorageType(6, 4092);
        skipListWrapper = new StorageType(57, 4047, 2);
        skipTailWrapper = new StorageType(8, 4096);
        offsetWrapper = new StorageType(8, 4096);
    }

    private void loadStores() throws IOException {
        recordWrapper.initialise(pageCache.map(new File(filename), recordStorePageSize, ANY_PAGE_SIZE));
        skipListWrapper.initialise(pageCache.map(new File(filename + ".meta"), skipListPageSize, ANY_PAGE_SIZE));
        skipTailWrapper.initialise(pageCache.map(new File(filename + ".nodep"), alignedPageSize, ANY_PAGE_SIZE));
        offsetWrapper.initialise(pageCache.map(new File(filename + ".offset"), alignedPageSize, ANY_PAGE_SIZE));
    }

    public void initialise(boolean createIfNotExists) {
        try {
            loadStores();
            PageCursor cursor = offsetWrapper.getReadPageCursor();

            while (cursor.next()) {
                long tempValue = cursor.getLong();
                offsets.add(tempValue);
            }
            if (offsets.size() > 0)
                currentOffset = offsets.get(offsets.size() - 1);
            PageCursor cursor1 = skipListWrapper.getReadPageCursor();
            highestNodeId = cursor1.getLong();
            highestEdgeId = cursor1.getLong();

        } catch (NoSuchFileException e) {
            if (createIfNotExists) {
                createStore();
            }
        } catch (IOException e) {
            throw new UnderlyingStorageException(e);
        }
    }

    private void createStore() {
        try {
            loadStores();
            offsets.add((long) 0);
            offsetFlush(0);
            PageCursor cursor = skipTailWrapper.getWritePageCursor();
            cursor.putLong(-1);
            cursor.putLong(-1);
            highestEdgeId = -1;
            highestNodeId = -1;
        } catch (IOException e) {
            throw new UnderlyingStorageException(e);
        }
    }

    private boolean sameMegaBlock(long a, long b){
        return (a>>20)==(b>>20);
    }
    void createSkipListBlock(long nodeId, long relationId) {
        if (!nodeOffset.containsKey(nodeId)) {
            long tail;
            if (nodeId >= highestNodeId) {
                skipTailWrapper.cleanRange(highestNodeId + 1, nodeId + 1);
                highestNodeId = nodeId;
                initialiseNodeBlock(currentOffset, nodeId, -1);
                currentOffset++;
            }
            try {
                PageCursor cursor = skipTailWrapper.getReadPageCursor();
                cursor = skipTailWrapper.moveCursorToBlock(nodeId, cursor);
                tail = cursor.getLong();
                if (tail == 0xffffffff||!sameMegaBlock(tail, currentOffset) ) {
                    long prev=-1;
                    if(tail!=0xffffffff){
                        prev=tail;
                    }
                    initialiseNodeBlock(currentOffset, nodeId, prev);
                    cursor = skipTailWrapper.moveCursorToBlock(nodeId, cursor);
                    cursor.putLong(currentOffset);
                    nodeOffset.put(nodeId, new OffsetData(currentOffset, 0));
                    currentOffset++;
                } else {
                    PageCursor cursor1 = skipListWrapper.getReadPageCursor();
                    cursor1 = skipListWrapper.moveCursorToBlock(tail, cursor1);
                    int count = cursor1.getByte(skipListWrapper.pageOffsetFromId(tail) + 8);
                    nodeOffset.put(nodeId, new OffsetData(tail, count));// read count from block!!!
                }
            } catch (IOException e) {
                throw new UnderlyingStorageException(e);
            }

        }
        try {
            PageCursor cursor = skipListWrapper.getWritePageCursor();
            if (nodeOffset.get(nodeId).count == 4) {
                long dataPoint = currentOffset++;
                initialiseNodeBlock(dataPoint, nodeId, nodeOffset.get(nodeId).begin);
                nodeOffset.get(nodeId).begin = dataPoint;
                nodeOffset.get(nodeId).count = 0;
            }
            OffsetData data = nodeOffset.get(nodeId);
            cursor = skipListWrapper.moveCursorToBlock(data.begin, cursor);
            cursor.putLong(skipListWrapper.pageOffsetFromId(data.begin) + nodeHeader + data.count * 8, relationId);
            data.count++;
            cursor.putByte(skipListWrapper.pageOffsetFromId(data.begin)+8,(byte) data.count);
        } catch (IOException e) {
            throw new UnderlyingStorageException(e);
        }
    }

    public void updateRecord(RelationshipRecord record) {
        byte lagging=0;
        if (record.isCreated()) {
            if ((record.getId() % 65536) == 0) {
                offsetFlush((int) (record.getId() / 65536)); //Why is the offset used?
            }

            createSkipListBlock(record.getFirstNode(), record.getId());
            int firstNode=(int)(nodeOffset.get(record.getFirstNode()).begin)&(0xffff);
            int firstOffset=nodeOffset.get(record.getFirstNode()).count;

            createSkipListBlock(record.getSecondNode(), record.getId());
            int secondNode=(int)(nodeOffset.get(record.getSecondNode()).begin)&(0xffff);
            int secondOffset=nodeOffset.get(record.getSecondNode()).count;
            byte[] data=new byte[5];
            data[0]=(byte)(firstNode&0xff);
            data[1]=(byte)((firstNode>>8)&0xff);
            data[2]=(byte)(secondNode&0xff);
            data[3]=(byte)((secondNode>>8)&0xff);
            data[4]=(byte) (((firstNode>>16)&0xf) +((secondNode>>16)&0xf)<<4);
            lagging=(byte)((secondOffset&0x3)<<4+(firstOffset&0x3)<<6);
        }
        updateEdgeRecordData(record,lagging);
    }

    /**
     * Record format: 48 bits, 0-3 second offset, 4-7: flags;
     * 8-23: First node id. 24-39: Second node relative id.
     * 40-45: first node offset, 46-48 second offset.
     *
     * @param id
     * @param record
     * @param mode
     * @return
     */

    public RelationshipRecord getRecord(long id, RelationshipRecord record, RecordLoad mode) {
        record.setId(id);
        try {
            readIntoRecord(id, record, mode);
        } catch (IOException e) {
            throw new UnderlyingStorageException(e);
        }
        return record;
    }

    void getRelationData(int node, int offset, boolean first, RelationshipRecord record) {
        try {
            PageCursor cursor = skipListWrapper.getReadPageCursor();
            cursor = skipListWrapper.moveCursorToBlock(node, cursor);
            do {
                long node1 = cursor.getLong();
                if (first) {
                    record.setFirstNode(node1);
                } else {
                    record.setSecondNode(node1);
                }
                int count = cursor.getByte();
                if (offset != 0 && (offset) < (count - 1)) {
                    long prevRelation = cursor.getLong(skipListWrapper.pageOffsetFromId(node) + (offset - 1) * 8 + nodeHeader);
                    long nextRelation = cursor.getLong(skipListWrapper.pageOffsetFromId(node) + (offset + 1) * 8 + nodeHeader);
                    if (first) {
                        record.setFirstPrevRel(prevRelation);
                        record.setFirstNextRel(nextRelation);
                    } else {
                        record.setSecondPrevRel(prevRelation);
                        record.setSecondNextRel(nextRelation);
                    }
                } else {
                    if (offset == 0) {
                        {
                            if ((first && record.isFirstInFirstChain()) || (!first && record.isFirstInSecondChain())) {
                                if (first) {
                                    record.setFirstPrevRel(-1);
                                } else {
                                    record.setSecondPrevRel(-1);
                                }
                            } else {
                                long page = cursor.getLong(skipListWrapper.pageOffsetFromId(node) + 9);
                                try {

                                    //PageCursor cursor2 = recordWrapper.getReadPageCursor();
                                    //do {
                                    cursor = skipTailWrapper.moveCursorToBlock(page, cursor);
                                    int count2 = cursor.getInt(skipListWrapper.pageOffsetFromId(page) + 8);
                                    long prev = cursor.getLong(skipListWrapper.pageOffsetFromId(page) + (count2 - 1) * 8 + nodeHeader);
                                    if (first) {
                                        record.setFirstPrevRel(prev);
                                    } else {
                                        record.setSecondPrevRel(prev);
                                    }
                                    //} while (cursor.shouldRetry());
                                } catch (IOException e) {
                                    throw new UnderlyingStorageException(e);
                                }
                            }
                        }
                    }
                    if ((offset) == (count - 1)) {
                        cursor = skipListWrapper.moveCursorToBlock(node, cursor);
                        long page = cursor.getLong(skipListWrapper.pageOffsetFromId(node) + 17);
                        try{
                            cursor = skipTailWrapper.moveCursorToBlock(page, cursor);
                            //(PageCursor
                        //} cursor2 = recordStore.io(0, PagedFile.PF_SHARED_READ_LOCK)) {
//                            do {
                            long prev = cursor.getLong(skipListWrapper.pageOffsetFromId(page)+ nodeHeader);
                            record.setFirstPrevRel(prev);
                            if (first) {
                                record.setFirstNextRel(prev);
                            } else {
                                record.setSecondNextRel(prev);
                            }
                        }catch (IOException e){
                            throw new UnderlyingStorageException(e);
                        }

                    }
                }

            } while (cursor.shouldRetry());
        } catch (IOException e) {
            throw new UnderlyingStorageException(e);
        }
    }

    //8+1+8+8+32

    public void readIntoRecord(long id, RelationshipRecord record, RecordLoad mode) throws IOException {
        PageCursor cursor = recordWrapper.getReadPageCursor();
        cursor = recordWrapper.moveCursorToBlock(id, cursor);
        int firstNode, firstOffset, secondNode, secondOffset;
        do {
            record.setInUse(false);
            byte header = cursor.getByte();
            boolean inUse = (header & 0x1) != 0;
            record.setInUse(inUse);
            if (mode.shouldLoad(inUse)) {
                record.setFirstInFirstChain((header & 0x2) != 0);
                record.setFirstInSecondChain((header & 0x4) != 0);
                byte[] data = new byte[5];
                cursor.getBytes(data, 0, 5);
                firstNode = (int) (data[0]) + (((int) data[1]) << 8)+((int) data[4]&0xf)<<16;
                firstNode+=offsets.get((int)(id>>20));
                secondNode = (int) (data[2]) + ((int) data[3]) << 8 +((int) data[4]&0xf0)<<12;
                secondNode+=offsets.get((int)(id>>20));
                firstOffset = (header>>6) & (0x3);
                secondOffset = (header>>4)&0x3;
                getRelationData(firstNode, firstOffset, true, record);
                getRelationData(secondNode, secondOffset, false, record);
            }
        } while (cursor.shouldRetry());
    }

    private void updateEdgeRecordData(RelationshipRecord record, byte lagging) {
        long id = record.getId();
        try {

            byte header = lagging;
            if (record.inUse()) {
                header = 1;
            }
            if (record.isFirstInFirstChain()) {
                header += 2;
            }
            if (record.isFirstInSecondChain()) {
                header += 4;
            }
            PageCursor cursor = recordWrapper.getWritePageCursor();
            cursor = recordWrapper.moveCursorToBlock(id, cursor);
            cursor.putByte(header);
        } catch (IOException e) {
            throw new UnderlyingStorageException(e);
        }
    }

    void initialiseNodeBlock(long offset, long nodeId, long prev) {
        PageCursor cursor = skipListWrapper.getReadPageCursor();
        try {
            cursor = skipTailWrapper.moveCursorToBlock(nodeId, cursor);
            cursor.putLong(offset);
            if (prev != -1) {
                cursor = skipListWrapper.moveCursorToBlock(prev, cursor);
                cursor.putLong(skipListWrapper.pageOffsetFromId(prev) + 17, offset);
            }
            cursor = skipListWrapper.moveCursorToBlock(offset, cursor);
            cursor.putLong(nodeId);

            cursor.putByte((byte) 0);
            cursor.putLong(prev);
            cursor.putLong(-1);

        } catch (IOException e) {
            throw new UnderlyingStorageException(e);
        }
    }

    void offsetFlush(int offset) {
        nodeOffset.clear();
        PageCursor cursor = offsetWrapper.getWritePageCursor();
        cursor.putLong(offset, currentOffset);
    }


    class OffsetData {
        long begin;
        int count;

        public OffsetData(long begin, int count) {
            this.begin = begin;
            this.count = count;
        }

    }
}
