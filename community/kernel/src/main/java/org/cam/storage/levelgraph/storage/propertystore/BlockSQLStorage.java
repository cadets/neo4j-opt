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

package org.cam.storage.levelgraph.storage.propertystore;

import java.sql.SQLException;
import java.util.ArrayList;

/**

    Implements a block based SQL store with multiple blocks, each block containing information about a single block.

    Edge properties are going to be block based.

    This is a pretty pointless class.

 */

public class BlockSQLStorage extends SQLDatabase {

    public BlockSQLStorage(String fileName) throws SQLException {
        super(fileName);
    }

    private String getBlockTable(int blockId) {
        return ((Integer) blockId).toString();
    }

    public void addNodeTable(int blockId) {
        internalAddNodeTable("nodetable" + getBlockTable(blockId));
    }

    public ArrayList<Long> getNodeIdsFromLabel(String type, String value, int blockid) {
        return internalGetNodeIdsFromLabel(type, value, getBlockTable(blockid));
    }

}
