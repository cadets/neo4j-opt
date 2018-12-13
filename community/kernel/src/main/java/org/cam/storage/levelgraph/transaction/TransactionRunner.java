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

import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.executionengine.StatementExecutor;
import org.cam.storage.levelgraph.storage.IdGenerator;

import java.util.Iterator;

public class TransactionRunner extends TransactionalStorage {

    //Currently, the results are not being checked from the cache. Also, inter transaction dependencies are not being handled. Sad.

    StatementExecutor statementExecutor;

    public TransactionRunner(Transaction transaction, IdGenerator nodeGen, IdGenerator relationGen) {
        super(nodeGen, relationGen);
        statementExecutor = new StatementExecutor(this);
    }


    public void executeTransaction() {
        Iterator<String> e = ((Transaction) transaction).getIterator();
        try {
            while (e.hasNext()) {
                statementExecutor.executeStatement(e.next());
            }
        } catch (Exception exception) {
            System.out.println("Exception occurred:" + exception.getMessage());
            transactionUpdatesCache.clearCache();
            transactionsResultsCache.clearCache();
        }
        //Add dependency checking here.
        if (false) {
            // Dependency checking failed.
            transactionUpdatesCache.clearCache();
            transactionsResultsCache.clearCache();
        } else {
            for (TransactionUpdatesCache.Updates update : transactionUpdatesCache.getAdditionList()
                    ) {
                switch (update.getUpdateType()) {
                    case Create:
                        if (update.getData() instanceof LevelNode) {
                            storage.addNode((LevelNode) update.getData());
                        } else {
                            if (update.getData() instanceof Edge) {
                                storage.addEdge((Edge) update.getData());
                            }
                        }
                        break;
                    case Update:
                        break;
                    case Delete:
                        //Should not go here
//                         if(update.getData() instanceof LevelNode){
//                            storage.deleteNode((LevelNode) update.getData(),getTransactionId());
//                        }else {
//                            if(update.getData() instanceof Edge){
//                                storage.deleteEdge((Edge) update.getData(),getTransactionId());
//                            }
//                        }
                        break;
                    default:
                        break;
                }
            }
            for (TransactionUpdatesCache.Updates update : transactionUpdatesCache.getDeletionList()
                    ) {
                switch (update.getUpdateType()) {
                    case Create:
                        //Should not go here
//                        if(update.getData() instanceof LevelNode){
//                            storage.addNode((LevelNode) update.getData());
//                        }else {
//                            if(update.getData() instanceof Edge){
//                                storage.addEdge((Edge) update.getData());
//                            }
//                        }
                        break;
                    case Update:
                        break;
                    case Delete:
                        if (update.getData() instanceof LevelNode) {
                            storage.deleteNode((LevelNode) update.getData(), getTransactionId());
                        } else {
                            if (update.getData() instanceof Edge) {
                                storage.deleteEdge((Edge) update.getData(), getTransactionId());
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
