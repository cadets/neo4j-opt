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

import org.cam.storage.levelgraph.datatypes.LevelNode;
import org.cam.storage.levelgraph.datatypes.StorableData;

import java.util.ArrayList;

public class MultiNodeResult implements StorableData{
    @Override
    public Long getDataId() {
        return (long)0;
    }

    ArrayList<LevelNode> levelNodes;

    @Override
    public byte[] toBytes() {
        return new byte[0]; //TODO only needed for sending over a network. Not useful now.
    }

    public ArrayList<LevelNode> getLevelNodes() {
        return levelNodes;
    }

    public void setLevelNodes(ArrayList<LevelNode> levelNodes) {
        this.levelNodes = levelNodes;
    }
}
