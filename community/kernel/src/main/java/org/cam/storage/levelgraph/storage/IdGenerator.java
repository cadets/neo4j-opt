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

package org.cam.storage.levelgraph.storage;

import com.google.common.primitives.Longs;
import org.cam.storage.levelgraph.Pair;
import org.cam.storage.levelgraph.storage.dataqueue.StackFile;

import java.io.File;
import java.io.IOException;

/**
 * Class providing the next set of ids to be used by the system.
 * Transaction running threads should take a block of ids and work on them.
 * These functions should only be called when a thread runs out of ids.
 * there is a possibility of returning Ids, but ideally should not be used.
 * In a distributed setup, blocks are to be divided on a much higher granularity.
 * The relative ordering of nodes using ids is inconsequential.
 * Better indexing would be a way forward for this.
 * @author jyothish
 */

public class IdGenerator {

    public static final long blockSize = 1000;
    long maxId;
    /**
     * File based stack used to store usable ids.
     */
    private StackFile permanentStorage;

    /**
     * @param file generate the idgnerator persisted on file.
     * @throws IOException from the stack file.
     */
    public IdGenerator(File file) throws IOException {
        permanentStorage = new StackFile.StackBuilder(file).build();
        if (permanentStorage.isEmpty()) {
            permanentStorage.add(Longs.toByteArray((long) 1));
            permanentStorage.add(Longs.toByteArray(blockSize + 1));
            maxId = 1;
        } else {
            maxId = Longs.fromByteArray(permanentStorage.readBottom());
        }
    }

    /**
     * The maxid is always at the bottom of the stack. If no unused id is available, return the maximum possible id.
     *
     * @return return the persisted maximum id.
     */

    private long getDataFromFile() {
        long result = maxId;
        try {
            result = Longs.fromByteArray(permanentStorage.peek());
            permanentStorage.remove();

        } catch (IOException e) {
            maxId += blockSize;
            return result;
        }
        return result;
    }

    /**
     * This is the to be called function in this class. A range of ids should be returned.
     * The size returned is hardcoded at {@value blockSize}
     *
     * @return a pair denoting the range of ids to be used. The end of range should not be used. l<=id<r notation is to be used.
     */
    public Pair<Long, Long> getIdRange() {
        long left = 0, right = 0;
        if (permanentStorage.size() == 2) {
            left = maxId;
            right = maxId + blockSize;
            maxId += blockSize;
        } else {
            try {
                left = Longs.fromByteArray(permanentStorage.peek());
                permanentStorage.remove();
                right = Longs.fromByteArray(permanentStorage.peek());
                permanentStorage.remove();
            } catch (IOException e) {
                left = maxId;
                right = maxId + blockSize;
                maxId += blockSize;
            }
        }
        return new Pair<Long, Long>(left, right);
    }

    /**
     * This function is ideally not to be used.
     * Always ask for a range of ids.
     *
     * @return a single id
     */
    private Long getNewId() {

        return maxId++;
    }

    /**
     * Return a range of ids {@code long} that couldn't be used by a transaction.
     * Both values are placed back in the permanent storage.
     *
     * @param low the lowest unused value.
     * @param high the maximum value unused.
     */
    public void returnIdRange(long low, long high) {
        try {
            permanentStorage.add(Longs.toByteArray(low));
        } catch (IOException e) {
            return;
        }
        try {
            permanentStorage.add(Longs.toByteArray(high));
        } catch (IOException e) {
            try {
                permanentStorage.remove();
            } catch (IOException e2) {
            }
        }
    }


}
