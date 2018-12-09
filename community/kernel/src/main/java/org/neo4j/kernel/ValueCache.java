/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel;


import org.neo4j.values.storable.Value;

import java.util.HashMap;

public class ValueCache {
    private HashMap<Long, HashMap<Integer, Value>> nodeLabelCache,
            nodePropertyCache,
            relationLabelCache,
            relationPropertyCache;

    public ValueCache() {
        nodeLabelCache = new HashMap<>();
        nodePropertyCache = new HashMap<>();
        relationLabelCache = new HashMap<>();
        relationPropertyCache = new HashMap<>();
    }

    private boolean collectionHasData(HashMap<Long, HashMap<Integer, Value>> map1, long var1, int var2) {
        if (map1.containsKey(var1)) {
            return map1.get(var1).containsKey(var2);
        } else {
            return false;
        }
    }

    private Value getCollectionData(HashMap<Long, HashMap<Integer, Value>> map1, long var1, int var2) {
        if (collectionHasData(map1, var1, var2)) {
            return map1.get(var1).get(var2);
        }
        return null;
    }

    private void addCollectiondata(HashMap<Long, HashMap<Integer, Value>> map1, long var1, int var2, Value var3) {
        map1.putIfAbsent(var1, new HashMap<>());
        map1.get(var1).putIfAbsent(var2, var3);
    }

    private boolean removeCollectionData(HashMap<Long, HashMap<Integer, Value>> map1, long var1, int var2) {
        if (map1.containsKey(var1)) {
            if (map1.get(var1).containsKey(var2)) {
                map1.get(var1).remove(var2);
                return true;
            }
        }
        return false;
    }

    public boolean nodeHasProperty(long nodeId, int propertyKeyId) {
        return collectionHasData(nodePropertyCache, nodeId, propertyKeyId);
    }

    public Value nodeGetProperty(long nodeId, int propertyKey) {
        return getCollectionData(nodePropertyCache, nodeId, propertyKey);
    }


    public boolean nodeHasLabel(long nodeId, int labelId) {
        return collectionHasData(nodeLabelCache, nodeId, labelId);
    }

    public Value nodeGetLabel(long nodeId, int labelId) {
        return getCollectionData(nodeLabelCache, nodeId, labelId);
    }


    public void nodeAddProperty(long nodeId, int propertyId, Value value) {
        addCollectiondata(nodePropertyCache, nodeId, propertyId, value);
    }

    public void nodeAddLabel(long nodeId, int propertyId, Value value) {
        addCollectiondata(nodeLabelCache, nodeId, propertyId, value);
    }

    public boolean relationHasProperty(long nodeId, int propertyKeyId) {
        return collectionHasData(relationPropertyCache, nodeId, propertyKeyId);
    }

    public Value relationGetProperty(long nodeId, int propertyKey) {
        return getCollectionData(relationPropertyCache, nodeId, propertyKey);
    }

    public boolean relationHasLabel(long nodeId, int labelId) {
        return collectionHasData(relationLabelCache, nodeId, labelId);
    }

    public Value relationGetLabel(long nodeId, int labelId) {
        return getCollectionData(relationLabelCache, nodeId, labelId);
    }

    public void relationAddProperty(long nodeId, int propertyId, Value value) {
        addCollectiondata(relationPropertyCache, nodeId, propertyId, value);
    }

    public void relationAddLabel(long nodeId, int propertyId, Value value) {
        addCollectiondata(relationLabelCache, nodeId, propertyId, value);
    }

    public boolean nodeRemoveProperty(long nodeId, int propertyKey) {
        return removeCollectionData(nodePropertyCache, nodeId, propertyKey);
    }

    public boolean nodeRemoveLabel(long nodeId, int labelKey) {
        return removeCollectionData(nodeLabelCache, nodeId, labelKey);
    }

    public boolean relationRemoveProperty(long relationId, int propertyKey) {
        return removeCollectionData(relationPropertyCache, relationId, propertyKey);
    }

    public boolean relationRemoveLabel(long relationId, int labelKey) {
        return removeCollectionData(nodePropertyCache, relationId, labelKey);
    }


}
