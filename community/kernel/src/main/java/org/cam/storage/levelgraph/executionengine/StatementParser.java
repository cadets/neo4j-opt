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

import org.cam.storage.levelgraph.datatypes.Edge;
import org.cam.storage.levelgraph.datatypes.LevelNode;

public class StatementParser {
    String[] getTokens(String s){
            return s.split("\\s");
    }

    LevelNode createNode(String s) {
        String [] tokens=s.split("\\s");
        if(tokens.length==1){
            throw (new RuntimeException("Empty create levelNode"));
        }
        //TODO: this assumes a numerical value of the id. A mapper to int
        return new LevelNode(Long.parseLong(tokens[1]));
    }

    LevelNode createNodeFromSubString(String s) {
        return new LevelNode(Long.parseLong(s));
    }

    Edge createEdge(String s){
        String [] tokens=s.split("\\s");
        if(tokens.length<4){
            throw(new RuntimeException("Create edge incorrect: "+s));
        }
        return new Edge(Long.parseLong(tokens[1]),Long.parseLong(tokens[2]),tokens[3].charAt(0),null);
    }
}
