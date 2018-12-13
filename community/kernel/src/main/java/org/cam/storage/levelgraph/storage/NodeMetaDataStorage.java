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

public class NodeMetaDataStorage {
    RocksDBInterface rocksDBInterface;

    NodeMetaDataStorage(RocksDBInterface rocksDBInterface1) {
        rocksDBInterface = rocksDBInterface1;
    }

    public Long getInternalId(Long externalId) {
        return rocksDBInterface.getValue(externalId, 0);
    }

    public void setInternalId(Long internalId, Long externalId) {
        rocksDBInterface.setValue(internalId, externalId, 0);
    }

    public void deleteNode(Long internalId) {
        //TODO: figure out range deletion for remove subtypes of a node.
        rocksDBInterface.deleteData(internalId, 0);
        rocksDBInterface.deleteData(internalId, 2);
        rocksDBInterface.deleteData(internalId, 3);
    }
}
