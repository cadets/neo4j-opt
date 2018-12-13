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
import org.cam.storage.levelgraph.datatypes.StorableData;

public class EmbeddedTransaction implements TransactionInterface {

    long transactionId;

    TransactionalStorage transactionalStorage;

    public EmbeddedTransaction() {
        transactionId = -1;
    }

    public void setTransactionalStorage(TransactionalStorage transactionalStorage) {
        this.transactionalStorage = transactionalStorage;
    }

    @Override
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public Long addEdge(Edge edge) {
        transactionalStorage.addEdge(edge);
        return null;
    }

    public Long addNode(LevelNode levelNode) {
        transactionalStorage.addNode(levelNode);
        return null;
    }

    public StorableData getEdges(LevelNode levelNode) {
        transactionalStorage.getEdges(levelNode);
        return null;
    }

    public void deleteNode(LevelNode levelNode) {
        transactionalStorage.deleteNode(levelNode);

    }

    public void findNode(LevelNode levelNode) {
        transactionalStorage.findNode(levelNode);
    }

    public void deleteEdge(Edge edge) {
        transactionalStorage.deleteEdge(edge);
    }

    public boolean commit() {
        transactionalStorage.commit();
        return true; //TODO: this should not be true all the time.
    }

    @Override
    public boolean begin() {
        return true; //TODO: relook at the semantics.
    }
}
