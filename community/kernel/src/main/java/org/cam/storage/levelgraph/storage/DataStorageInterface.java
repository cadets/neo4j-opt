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

import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.neo4j.values.storable.Value;

import java.util.ArrayList;

public interface DataStorageInterface {

    /*
    Long nodeCreate();

    void nodeDelete(Long id);

    int nodeDetachDelete(Long id);

    long relationshipCreate(int relationType, long start, long end);

    void relationshipDelete(int relationId);


    boolean nodeAddLabel(long nodeId, int labelId);

    boolean nodeRemoveLabel(long nodeId, int labelId);


    Value nodeSetProperty(long relationshipId, int propertyKeyId, Value value);

    Value nodeRemoveProperty(long nodeId, int propertyKeyId);

    Value relationshipSetProperty(long nodeId, int propertyKeyid, Value value);

    Value relationshipRemoveProperty(long relationshipId, int propertyKeyId);

    PrimitiveLongIterator nodesGetAll();
*/


    Long addNode(LevelNode levelNode);

    Long addEdge(Long from, Edge edge);


    ArrayList<Edge> getEdges(LevelNode levelNode, long queryingTransaction);

    ArrayList<Value> getProperties(LevelNode levelNode);

    Value getNodeProperty(int propertyId);

    Value getRelationshipProperty(int propertyId);

    ArrayList<Value> getProperties(Edge edge);

    void addProperty(LevelNode node, Value properties, int id);

    void addProperty(Edge edge, Value properties, int id);

    //Delete operations will be implemented later. Right now, stale state will be permeated.
}

