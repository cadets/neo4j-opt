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


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


/*
    LevelNode data is stored in the SQL database, the specific edge properties are going to be stored close to the edge in the edge store.

    A LRU Cache of most recently requested data is kept here.

    This class is on the boundary of storage and query management.

    All data in the SQL database is going to be stored locally close to the nodes and edges as well.

    SQL db is to find the nodes with certain properties. The data close to nodes is to perform levelNode centric checks.

    Duplication of data increases the space utilisation, but increases the performance of queries.

 */


public class SQLDatabase {

    private ArrayList<String> headers;
    private Connection conn;

    /*
    Creates an sql backend to keep track of LevelNode properties.

    @param filename specifies the name of the db file, including the filepath

     */

    public SQLDatabase(String filename) throws SQLException {
        conn = DriverManager.getConnection(filename);
        headers = new ArrayList<>();
    }

    protected void createTableVarArgs(String tableName, ArrayList<String> tableDetails) {

        String sql = "CREATE TABLE IF NOT EXISTS" + tableName + "(id integer PRIMARY KEY,name text PRIMARY KEY";

        headers.add("id");
        headers.add("name");

        for (String str : tableDetails) {
            headers.add(str);
            sql += str + ",";
        }
        sql += ");";

        try {
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

    }

    /*
    Creates a table with variable number of arguments.

    @param tableName the name of the sql table for a given database.

    @param tableDetails specifies the various properties that a levelNode can have, and the data format


     */

    protected void createTableVarArgs(ArrayList<String> tableDetails) {
        createTableVarArgs("default", tableDetails);
    }

    protected void internalAddNodeTable() {
        internalAddNodeTable("default");
    }

    protected void internalAddNodeTable(String tablename) {

        String sql = "CREATE TABLE IF NOT EXISTS " + tablename + " (id integer PRIMARY KEY,name text PRIMARY KEY);";

        headers.add("id");
        headers.add("name");

        try {

            conn.createStatement().execute(sql);

        } catch (SQLException e) {

            System.err.println(e.getMessage());

        }
    }

    protected ArrayList<Long> internalGetNodeIdsFromLabel(String type, String value) {
        return internalGetNodeIdsFromLabel(type, value, "default");
    }

    public ArrayList<String> getNodeProperties(Long nodeId, String tablename) {
        String sql = "SELECT * from " + tablename + " WHERE id =" + nodeId;
        try {
            ResultSet results = conn.createStatement().executeQuery(sql);
            ArrayList<String> ids = new ArrayList<>();
            for (String s : headers
            ) {
                ids.add(results.getString(s));
            }
            return ids;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    protected ArrayList<Long> internalGetNodeIdsFromLabel(String type, String value, String tablename) {
        String sql = "SELECT id FROM" + tablename + "WHERE" + type + "=" + value;
        try {
            ResultSet results = conn.createStatement().executeQuery(sql);
            ArrayList<Long> ids = new ArrayList<>();
            while (results.next()) {
                ids.add(results.getLong("id"));
            }
            return ids;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public Long getNodeIdFromName(String nodeName, String tablename) {
        String sql = "SELECT id from " + tablename + " WHERE name= " + nodeName;
        try {
            ResultSet results = conn.createStatement().executeQuery(sql);
            return results.getLong("id");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    /*
        Result set needs to be managed in this.
     */
    public class LRUCache<K, V> extends LinkedHashMap<K, V> {

        short hits, counter, windowSize;
        double targetHitRate;
        private int cacheSize, maxCacheSize;

        public LRUCache(int cacheSize) {
            super(16, (float) 0.75, true);
            this.cacheSize = cacheSize;
            windowSize = 10000;
            targetHitRate = 0.5;
            maxCacheSize = 100000000;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() >= cacheSize;
        }

        public void updateCacheSize(int newCacheSize) {
            cacheSize = newCacheSize;
        }

        public boolean containsKey(Object value) {

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


        public V get(Object key) {
            if (containsKey(key)) {
                return super.get(key);
            }
            return null;
        }
    }


}
