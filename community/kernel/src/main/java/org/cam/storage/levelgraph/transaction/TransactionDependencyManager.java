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

import java.util.HashMap;
import java.util.Iterator;

public class TransactionDependencyManager {

    private DependencyGraph dependencyGraph;
    HashMap<Long, Transaction> transactionHashMap;

    public TransactionDependencyManager(){
        dependencyGraph=new DependencyGraph();
        transactionHashMap=new HashMap<>();
    }

    public void addTransaction(Transaction transaction){
        dependencyGraph.addNode(transaction.getTransactionId());
        transactionHashMap.put(transaction.getTransactionId(),transaction);
    }

    public void removeTransaction(Transaction transaction){
        dependencyGraph.deleteNode(transaction.getTransactionId());
        transactionHashMap.remove(transaction.getTransactionId());
    }

    private boolean addAndCheckDependency(Transaction from, Transaction to){
        return dependencyGraph.addDirectedEdge(from.getTransactionId(),to.getTransactionId());
    }

    /*

    Adding transaction dependency and removing the transaction with
    the highest out degree. The transaction which has the highest chance of
    creating more cycles.

     */
    public Transaction addDependency(Transaction from, Transaction to){
        Transaction result=null;
        int maxDegree=0;
        if(addAndCheckDependency(from, to)){
            Iterator<Long> cycleNodes=dependencyGraph.getCycleNodes();
            while (cycleNodes.hasNext()) {
                Transaction node=transactionHashMap.get(cycleNodes.next());
                if(!node.isFinished()){
                    if (maxDegree < dependencyGraph.getNodeOutDegree(node.getTransactionId())) {
                        result=node;
                        maxDegree=dependencyGraph.getNodeOutDegree(node.getTransactionId());
                    }
                }
            }
        }
        return result;
    }


}
