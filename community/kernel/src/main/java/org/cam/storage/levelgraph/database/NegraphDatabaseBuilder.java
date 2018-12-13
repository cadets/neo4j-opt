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

package org.cam.storage.levelgraph.database;

import org.cam.storage.levelgraph.storage.dataqueue.WriteAheadLog;
import org.cam.storage.levelgraph.storage.ondiskstorage.UnifiedDataStorage;
import org.cam.storage.levelgraph.storage.ondiskstorage.UnifiedDataStorageBuilder;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class NegraphDatabaseBuilder {
    WriteAheadLog logger;
    AtomicInteger transactionCounter;
    UnifiedDataStorage storage;
    private String baseFolder;

    public NegraphDatabaseBuilder setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
        String storagePath = baseFolder + "/data";
        String logPath = baseFolder + "/log";
        File directory = new File(logPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        logger = new WriteAheadLog(logPath + "/wal.log");
        storage = new UnifiedDataStorageBuilder().setPath(storagePath).createUnifiedDataStorage();
        transactionCounter = new AtomicInteger();
        return this;
    }

    public NegraphDatabase createNegraphDatabase() {
        NegraphDatabase session = new NegraphDatabase()
                .setLogger(logger)
                .setStorage(storage)
                .setTransactionCounter(transactionCounter);
        return session;
    }

}