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

package org.cam.storage.levelgraph.executionengine;

import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.datatypes.StorableData;
import org.cam.storage.levelgraph.transaction.TransactionRunner;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class StatementExecutor extends StatementParser{

    public static final Map<String, Method> commands = new HashMap<>();

    static {
        try {
            commands.put("AddEdge", StatementExecutor.class.getMethod("addEdge", String.class));
            commands.put("DeleteEdge", StatementExecutor.class.getMethod("deleteEdge", String.class));
            commands.put("AddNode", StatementExecutor.class.getMethod("addNode", String.class));
            commands.put("DeleteNode", StatementExecutor.class.getMethod("deleteNode", String.class));
            commands.put("AddEdgeProperty", StatementExecutor.class.getMethod("addEdgeProperty", String.class));
            commands.put("FindEdge", StatementExecutor.class.getMethod("findEdge", String.class));
            commands.put("FindNode", StatementExecutor.class.getMethod("findNode", String.class));
        }
        catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }

    }

    TransactionRunner transactionRunner;


    public StatementExecutor(TransactionRunner runner) {
        transactionRunner = runner;
    }

    public void executeStatement(String s) {
        String [] tokens=getTokens(s);
        try {
            commands.get(tokens[0]).invoke(s);
        }catch(Exception e){
            System.err.println(e.toString());
        }
    }

    public void addEdge(String s) {
        transactionRunner.addEdge(createEdge(s));
    }

    public void deleteEdge(String s) {
        transactionRunner.deleteEdge(createEdge(s));
    }

    public void addNode(String s) {
        transactionRunner.addNode(createNode(s));
    }

    public void deleteNode(String s) {
        //TODO has not been properly implemented.
        transactionRunner.deleteNode(createNode(s));
    }

    public void addEdgeProperty(String s) {
        //TODO right now the db is append only. Need to make it a better one.
    }

    public StorableData findEdge(String s) {
        return transactionRunner.getEdges(createNode(s));
    }

    public StorableData findNode(String s) {
        LevelNode levelNode = createNode(s);
        transactionRunner.findNode(levelNode);
        return levelNode;
    }



}
