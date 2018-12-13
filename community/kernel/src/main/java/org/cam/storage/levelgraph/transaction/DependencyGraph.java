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

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Iterator;

public class DependencyGraph {
    Graph<Long, DefaultEdge> g;
    CycleDetector<Long, DefaultEdge> cycleDetector;

    public DependencyGraph() {
        g = new DefaultDirectedGraph<Long, DefaultEdge>(DefaultEdge.class);
        CycleDetector<Long, DefaultEdge> cycleDetector = new CycleDetector<>(g);
    }

    public void addNode(Long node) {
        g.addVertex(node);
    }

    public boolean addDirectedEdge(Long from, Long to) {
        g.addEdge(from, to);
        return cycleDetector.detectCycles();
    }

    public void deleteNode(Long node) {
        g.removeVertex(node);
    }

    /*
        Should only be called after detect cycles succeeded
     */
    public Iterator<Long> getCycleNodes() {
        return cycleDetector.findCycles().iterator();
    }

    public int getNodeDegree(Long node){
        return g.degreeOf(node);
    }

    public int getNodeOutDegree(Long node){
        return g.outDegreeOf(node);
    }

}
