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
import java.util.LinkedHashMap;
import java.util.Map;

public class ValueCache {
    private LRUCache<Long, HashMap<Integer, Value>> nodeLabelCache,
            nodePropertyCache,
            relationLabelCache,
            relationPropertyCache;
    int counterRelationGetProp, counterRelationPutProp, counterNodeGetProp, counterNodeSetProp;

    public ValueCache() {
        nodeLabelCache = new LRUCache<>(100000);
        nodePropertyCache = new LRUCache<>(100000);
        relationLabelCache = new LRUCache<>(100000);
        relationPropertyCache = new LRUCache<>(100000);
        counterNodeGetProp=0;
        counterNodeSetProp=0;
        counterRelationGetProp=0;
        counterRelationPutProp=0;
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

    private void addCollectionData(HashMap<Long, HashMap<Integer, Value>> map1, long var1, int var2, Value var3) {
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
        counterNodeGetProp++;
        return getCollectionData(nodePropertyCache, nodeId, propertyKey);
    }


    public boolean nodeHasLabel(long nodeId, int labelId) {
        return collectionHasData(nodeLabelCache, nodeId, labelId);
    }

    public Value nodeGetLabel(long nodeId, int labelId) {
        return getCollectionData(nodeLabelCache, nodeId, labelId);
    }


    public void nodeAddProperty(long nodeId, int propertyId, Value value) {
        counterNodeSetProp++;
        addCollectionData(nodePropertyCache, nodeId, propertyId, value);
    }

    public void nodeAddLabel(long nodeId, int propertyId, Value value) {
        addCollectionData(nodeLabelCache, nodeId, propertyId, value);
    }

    public boolean relationHasProperty(long nodeId, int propertyKeyId) {
        return collectionHasData(relationPropertyCache, nodeId, propertyKeyId);
    }

    public Value relationGetProperty(long nodeId, int propertyKey) {
        counterRelationGetProp++;
        return getCollectionData(relationPropertyCache, nodeId, propertyKey);
    }

    public boolean relationHasLabel(long nodeId, int labelId) {
        return collectionHasData(relationLabelCache, nodeId, labelId);
    }

    public Value relationGetLabel(long nodeId, int labelId) {
        return getCollectionData(relationLabelCache, nodeId, labelId);
    }

    public void relationAddProperty(long nodeId, int propertyId, Value value) {
        counterRelationPutProp++;
        addCollectionData(relationPropertyCache, nodeId, propertyId, value);
    }

    public void relationAddLabel(long nodeId, int propertyId, Value value) {
        addCollectionData(relationLabelCache, nodeId, propertyId, value);
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

    public class LRUCache<K, V> extends LinkedHashMap<K, V> {

        short hits, counter, windowSize;
        double targetHitRate;
        private int cacheSize, maxCacheSize;

        public LRUCache(int cacheSize) {
            super(16, (float) 0.75, true);
            this.cacheSize = cacheSize;
            windowSize = 10000;
            targetHitRate = 0.5;
            maxCacheSize = 10000000;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() >= cacheSize;
        }

        public void updateCacheSize(int newCacheSize) {
            cacheSize = newCacheSize;
        }

        public boolean containsNoKey(Object value) {

            boolean result = super.containsKey(value);

            counter++;

            if (result) {
                hits++;
            }

            if (counter == windowSize) {
                counter = 0;
                hits = 0;

                double hitRate = (float) hits / (float) windowSize;
                if (hitRate < targetHitRate) {
                    if ((cacheSize * 2) < maxCacheSize)
                        updateCacheSize(cacheSize * 2);
                }
                if (hitRate > (targetHitRate * 2)) {
                    if (cacheSize > 1000000)
                        updateCacheSize(cacheSize / 2);
                }
            }
            return result;
        }


        public V getto(Object key) {
            if (containsKey(key)) {
                return super.get(key);
            }

            return null;
        }
    }



}
