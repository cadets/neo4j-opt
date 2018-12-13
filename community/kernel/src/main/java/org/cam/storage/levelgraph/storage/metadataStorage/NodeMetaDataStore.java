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

package org.cam.storage.levelgraph.storage.metadataStorage;

import org.cam.storage.levelgraph.storage.RocksDBInterface;

/**
 * Node data such as first and last occurrence, internal id to external id mapping and subnodes are stored here
 */


public class NodeMetaDataStore {

    // 0: LevelNode external to internal
    // 1: LevelNode, Edge type to Sub-levelNode
    // 2: LevelNode to start buffer
    // 3: LevelNode to last buffer

    RocksDBInterface rocksDBInterface;

    public NodeMetaDataStore(RocksDBInterface dbInterface) {
        rocksDBInterface = dbInterface;
    }

    public Long getInternalId(String externalId) {
        return rocksDBInterface.getValue(externalId, 0);
    }

    public void setInternalId(String externalId, Long internalId) {
        rocksDBInterface.setValue(externalId, internalId, 0);
    }

    public Long getStartBuffer(Long internalId) {
        return rocksDBInterface.getValue(internalId, 2);
    }

    public Long getLastBuffer(Long internalId) {
        return rocksDBInterface.getValue(internalId, 3);
    }

    public void setStartBuffer(Long internalId, Long bufferId) {
        rocksDBInterface.setValue(internalId, bufferId, 2);
    }

    public void setEndBuffer(Long internalId, Long bufferId) {
        rocksDBInterface.setValue(internalId, bufferId, 3);
    }

}
