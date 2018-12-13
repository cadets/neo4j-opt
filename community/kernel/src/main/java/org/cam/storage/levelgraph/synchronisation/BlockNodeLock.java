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

package org.cam.storage.levelgraph.synchronisation;

public class BlockNodeLock {
    Long blockId, NodeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockNodeLock)) return false;

        BlockNodeLock that = (BlockNodeLock) o;

        if (!blockId.equals(that.blockId)) return false;
        return NodeId.equals(that.NodeId);
    }

    @Override
    public int hashCode() {
        int result = blockId.hashCode();
        result = 31 * result + NodeId.hashCode();
        return result;
    }
}
