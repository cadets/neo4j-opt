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

import org.cam.storage.levelgraph.storage.IdGenerator;
import org.cam.storage.levelgraph.storage.dataqueue.WriteAheadLog;
import org.cam.storage.levelgraph.storage.ondiskstorage.UnifiedDataStorage;
import org.cam.storage.levelgraph.storage.ondiskstorage.UnifiedDataStorageBuilder;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class EmbeddedSession {

    WriteAheadLog logger;

    AtomicInteger transactionCounter;

    UnifiedDataStorage storage;

    IdGenerator nodegen, relationGen;

    EmbeddedSession(WriteAheadLog logger, UnifiedDataStorage storage, IdGenerator nodegen, IdGenerator relationGen, AtomicInteger transactionCounter){
        this.logger=logger;
        this.storage=storage;
        this.nodegen=nodegen;
        this.relationGen=relationGen;
        this.transactionCounter=transactionCounter;
    }

    public void setLogger(WriteAheadLog logger) {
        this.logger = logger;
    }

    public void setTransactionCounter(AtomicInteger transactionCounter) {
        this.transactionCounter = transactionCounter;
    }

    public void setStorage(UnifiedDataStorage storage) {
        this.storage = storage;
    }


    EmbeddedTransaction getNewTransaction() {
        EmbeddedTransaction transaction = new EmbeddedTransaction();
        transaction.setTransactionId(transactionCounter.longValue());
        TransactionalStorage temp = new TransactionalStorage(nodegen,relationGen);
        temp.setStorage(storage);
        temp.setWal(logger);
        transaction.setTransactionalStorage(temp);
        return transaction;
    }

}
