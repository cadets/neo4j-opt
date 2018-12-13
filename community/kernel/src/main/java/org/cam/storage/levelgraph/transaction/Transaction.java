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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Implementation of the TransactionInterface. This class has support for snapshots and locking as of now.
 */

public class Transaction implements TransactionInterface {

    private Long transactionId;

    private ArrayList<String> statementList;

    private Long snapshotId;

    private boolean finished;

    Transaction(TransactionScheduler scheduler){
        transactionId=scheduler.getCurrentTransactionId();
        finished=false;
    }

    Transaction(TransactionScheduler scheduler, ArrayList<String> statementList){
        this(scheduler);
        this.statementList=statementList;
    }

    Iterator<String> getIterator() {
        return statementList.iterator();
    }

    public Long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Long snapshotId) {
        this.snapshotId = snapshotId;
    }

    public ArrayList<String> getStatementList() {
        return statementList;
    }

    public void setStatementList(ArrayList<String> statementList) {
        this.statementList = statementList;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long tid){
        transactionId=tid;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public boolean commit() {
        return false;
    }

    @Override
    public boolean begin() {
        return false;
    }
}
