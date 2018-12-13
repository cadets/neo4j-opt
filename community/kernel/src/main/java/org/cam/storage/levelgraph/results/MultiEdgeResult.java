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

package org.cam.storage.levelgraph.results;

import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.StorableData;

import java.util.ArrayList;

public class MultiEdgeResult implements StorableData{
    ArrayList<Edge> edges;

    @Override
    public Long getDataId() {
        return (long)0;
    }

    public MultiEdgeResult(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0]; //Results are towards user, so no need to serialise it as of now. For sending over network, this would be needed. TODO
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }
}
