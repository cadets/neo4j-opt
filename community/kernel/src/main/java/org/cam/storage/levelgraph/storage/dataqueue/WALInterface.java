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

package org.cam.storage.levelgraph.storage.dataqueue;

import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.datatypes.PropertyEntity;

public interface WALInterface {

    void addData(byte[] data);

    byte[] getData();

    void clear();

    boolean isFull();

    void addNode(LevelNode levelNode);

    void addEdge(Edge edge);

    void addNodeProperty(LevelNode levelNode, PropertyEntity properties);

    void addEdgeProperty(Edge edge, PropertyEntity properties);

    void deleteNode(LevelNode levelNode);

    void deleteEdge(Edge edge);

    void deleteNodeProperty(LevelNode levelNode, PropertyEntity properties);

    void deleteEdgeProperty(Edge edge, PropertyEntity properties);

    boolean isEmpty();

    byte[] getNext();

}
