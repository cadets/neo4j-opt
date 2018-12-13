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

public class SQLNodePropertyStore implements NodePropertyStoreInterface {

    SQLDatabase database;

    public SQLNodePropertyStore(String filePath) {
        try {
            database = new SQLDatabase(filePath + "/nodeDb.db");
        } catch (SQLException e) {
            System.err.println("SQL Database not opened");
        }
    }


    @Override
    public void addNodeProperty(Long nodeid, String property, String value) {

    }

    @Override
    public void updateNodeProperty(Long nodeId, String propertyName, String value) {

    }

    @Override
    public String getNodeProperty(Long nodeId, String propertyName) {
        return null;
    }

    @Override
    public ArrayList<Long> getNodesWithProperty(String propertyName, String value) {
        return null;
    }

    @Override
    public boolean startTransaction() {
        return false;
    }

    @Override
    public boolean commitTransaction() {
        return false;
    }
}
