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

import org.cam.storage.levelgraph.storage.IdGenerator;
import org.cam.storage.levelgraph.storage.dataqueue.WriteAheadLog;
import org.cam.storage.levelgraph.storage.ondiskstorage.UnifiedDataStorage;
import org.cam.storage.levelgraph.storage.ondiskstorage.UnifiedDataStorageBuilder;
import org.cam.storage.levelgraph.transaction.EmbeddedTransaction;
import org.cam.storage.levelgraph.transaction.TransactionalStorage;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class NegraphDatabase {

    WriteAheadLog logger;

    AtomicInteger transactionCounter;

    UnifiedDataStorage storage;

    IdGenerator nodeGen, relationGen;

    NegraphDatabase() {

    }

    NegraphDatabase(String baseFolder) {
        String storagePath = baseFolder + "/data";
        String logPath = baseFolder + "/log";
        String idgenPath = baseFolder + "/meta";
        File directory = new File(logPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        logger = new WriteAheadLog(logPath + "/wal.log");
        storage = new UnifiedDataStorageBuilder().setPath(storagePath).createUnifiedDataStorage();
        transactionCounter = new AtomicInteger();
        try {
            nodeGen = new IdGenerator(new File(idgenPath + "/node.dat"));
            relationGen = new IdGenerator(new File(idgenPath + "/relation.dat"));
        } catch (IOException e) {
            System.out.println("Idgen could not be opened");
        }
    }

    public NegraphDatabase setIdGens(IdGenerator nodeGen, IdGenerator relationGen) {
        this.nodeGen = nodeGen;
        this.relationGen = relationGen;
        return this;
    }
    public NegraphDatabase setLogger(WriteAheadLog logger) {
        this.logger = logger;
        return this;
    }

    public NegraphDatabase setTransactionCounter(AtomicInteger transactionCounter) {
        this.transactionCounter = transactionCounter;
        return this;
    }

    public NegraphDatabase setStorage(UnifiedDataStorage storage) {
        this.storage = storage;
        return this;
    }


    EmbeddedTransaction getNewTransaction() {
        EmbeddedTransaction transaction = new EmbeddedTransaction();
        transaction.setTransactionId(transactionCounter.longValue());
        TransactionalStorage temp = new TransactionalStorage(nodeGen, relationGen);
        temp.setStorage(storage);
        temp.setWal(logger);
        transaction.setTransactionalStorage(temp);
        return transaction;
    }

}
