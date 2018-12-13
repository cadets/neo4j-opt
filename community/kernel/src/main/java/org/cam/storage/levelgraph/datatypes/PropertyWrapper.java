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

package org.cam.storage.levelgraph.datatypes;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import java.util.HashMap;
import java.util.Map;

public class PropertyWrapper implements PropertyContainer {
    HashMap<String, PropertyEntity> properties;

    GraphDatabaseService dbService;

    @Override
    public GraphDatabaseService getGraphDatabase() {
        return dbService;
    }

    @Override
    public boolean hasProperty(String s) {
        return properties.containsKey(s);
    }

    @Override
    public Object getProperty(String s) {
        return properties.get(s);
    }

    @Override
    public Object getProperty(String s, Object o) {
        return o = getProperty(s);
    }

    @Override
    public void setProperty(String s, Object o) {
        properties.put(s, new PropertyEntity(o.toString().getBytes()));
    }

    @Override
    public Object removeProperty(String s) {
        return properties.remove(s);
    }

    @Override
    public Iterable<String> getPropertyKeys() {
        return properties.keySet();
    }

    @Override
    public Map<String, Object> getProperties(String... strings) {
        HashMap<String, Object> result = new HashMap<>();
        for (String string : strings) {
            if (properties.containsKey(string)) {
                result.put(string, properties.get(string));
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> getAllProperties() {
//        return properties;
        //TODO: find a proper way to do this
        return null;
    }
}
