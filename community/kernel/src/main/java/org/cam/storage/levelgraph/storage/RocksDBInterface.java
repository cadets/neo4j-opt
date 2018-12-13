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


import com.google.common.primitives.Longs;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * RocksDB holds persistent metadata and Node data.
 */


public class RocksDBInterface {

    private static final String[] names = {"ettoin", "edgetype", "startbuffer", "lastbuffer", "buffercount", "ne4jIdToNeGraphId"};

    static {
        RocksDB.loadLibrary();
    }

    private final List<ColumnFamilyHandle> columnFamilyHandles;

    private RocksDB db;
    // 0: LevelNode external to internal
    // 1: LevelNode, Edge type to Sub-levelNode
    // 2: LevelNode to start buffer
    // 3: LevelNode to last buffer
    // 4: Last used bufferCount

    public RocksDBInterface(String dbPath, int columns) {
        final List<ColumnFamilyDescriptor> columnFamilyDescriptors =
                new ArrayList<>();
        columnFamilyHandles = new ArrayList<>();
        String databasePath = dbPath + "/data";
        File directory = new File(databasePath);
        if (! directory.exists()){
            directory.mkdirs();
        }
        try {
            Options options = new Options();
            RocksDB rocksDB;
            options.setCreateIfMissing(true);
            options.setErrorIfExists(true);

            try {
                rocksDB = RocksDB.open(options, databasePath);
                for (int i = 1; i < columns; i++)
                    rocksDB.createColumnFamily(new ColumnFamilyDescriptor(("cFamily" + names[i]).getBytes(), new ColumnFamilyOptions()));
                rocksDB.close();
            } catch (RocksDBException e) {
                System.out.println(e.getMessage());
            }

            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(
                    RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
            try {
                for (int i = 1; i < columns; i++) {
                    columnFamilyDescriptors.add(
                            new ColumnFamilyDescriptor(("cFamily" + names[i]).getBytes(), new ColumnFamilyOptions())
                    );
                }
            } catch (Exception e) {
                System.out.println(e.getMessage() + "Coloumn descriptor forced and error");
            }


            final DBOptions optionsNew = new DBOptions();
            optionsNew.allowMmapWrites();
            optionsNew.allowMmapReads();
            optionsNew.allow2pc();
            optionsNew.setErrorIfExists(false);
            db = RocksDB.open(optionsNew, databasePath,
                    columnFamilyDescriptors, columnFamilyHandles);
            assert (db != null);
            System.out.println(columnFamilyHandles.size());
            assert (db != null);
        } catch (RocksDBException e) {
            System.err.println(e.toString() + "RocksDB Not working");
        }
    }

    @NotNull
    byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    @NotNull
    @Contract(pure = true)
    private byte[] stringToBytes(String s) {
        return s.getBytes();
    }

    private boolean keyMayExist(byte[] key, int id){
        StringBuilder retValue = new StringBuilder();
        return db.keyMayExist(columnFamilyHandles.get(id), key, retValue);
    }
    private byte[] getRawValue(byte[] key, int id) throws RocksDBException {
        try {
            StringBuilder retValue = new StringBuilder();
            if (db.keyMayExist(columnFamilyHandles.get(id), key, retValue)) {
                return db.get(columnFamilyHandles.get(id), key);
            } else {
                throw new RocksDBException("Key not found");
            }
        } catch (RocksDBException e) {
            System.err.println("Key search failed for key:" + key.toString());
            throw e;
        }
    }

    private void setRawValue(byte[] key, byte[] value, int id) {
        try {
            db.put(columnFamilyHandles.get(id), key, value);
        } catch (RocksDBException e) {
            System.err.println("Rocks DB insertion failed ");
        }
    }

    public void cleanUpDataBase() {
        for (int i = 0; i < columnFamilyHandles.size(); i++) {
            try {
                db.dropColumnFamily(columnFamilyHandles.get(i));
            } catch (Exception e) {
                System.err.println("RocksDB column drop failed");
            }
        }
    }

    public Long getValue(String key, int id) {
        try {
            return Longs.fromByteArray(getRawValue(stringToBytes(key), id));
        } catch (RocksDBException e) {
            return (long)-1;
        }
    }

    public boolean keyMayExist(Long key, int id){
        return keyMayExist(longToBytes(key),id);
    }

     public boolean keyMayExist(String key, int id){
        return keyMayExist(stringToBytes(key),id);
    }


    public Long getValue(Long key, int id) {
        try {
            byte[] result = getRawValue(longToBytes(key), id);
            if (result == null) {
                return (long) -1;
            }
            return ByteBuffer.wrap(result).getLong();
        } catch (RocksDBException e) {
            return (long) -1;
        }
    }

    public String getStringValue(Long key, int id){
         try {
            byte[] result = getRawValue(longToBytes(key), id);
            if (result == null) {
                return null;
            }
            return ByteBuffer.wrap(result).toString();
        } catch (RocksDBException e) {
            return null;
        }
    }

    public Long getValue(Long key, String key2, int id) {
        try {
            return ByteBuffer.wrap(getRawValue(ArrayUtils.addAll(longToBytes(key), stringToBytes(key2)), id)).getLong();
        } catch (RocksDBException e) {
            return (long) -1;
        }
    }

    public void setValue(Long key, String value, int id){
        setRawValue(Longs.toByteArray(key), value.getBytes(),id);
    }
    public void setValue(Long key, Long value, int id) {
        setRawValue(longToBytes(key), longToBytes(value), id);
    }

    public void setValue(String key, Long value, int id) {
        setRawValue(stringToBytes(key), longToBytes(value), id);
    }

    public void setValue(Long key, String key2, Long value, int id) {
        setRawValue(ArrayUtils.addAll(longToBytes(key), stringToBytes(key2)), longToBytes(value), id);
    }

    void deleteData(String data, int id) {
        try {
            db.delete(columnFamilyHandles.get(id), stringToBytes(data));
        } catch (RocksDBException e) {
            System.out.println("Deletion failed");
        }
    }

    void deleteData(Long data, int id) {
        try {
            db.delete(columnFamilyHandles.get(id), longToBytes(data));
        } catch (RocksDBException e) {
            System.out.println("Deletion failed");
        }
    }
    public void setNodeInternalId(String externalId, Long internalId) {
        setValue(externalId, internalId, 0);
    }

    public void setNodeStartBuffer(Long internalId, Long bufferId) {
        setValue(internalId, bufferId, 2);
    }

    public void setNodeLastBuffer(Long internalId, Long bufferId) {
        setValue(internalId, bufferId, 3);
    }

    public Long getNodeInternalId(String externalId) {
        return getValue(externalId, 0);
    }

    public Long getNodeStartBuffer(Long internalId) {
        return getValue(internalId, 2);
    }

    public Long getNodeLastBuffer(Long internalId) {
        return getValue(internalId, 3);
    }

    public Long incrementBufferId(Long increment) {
        Long value;
        value = getValue((long) 0, 4);
        if (value == -1)
            value = (long) 0;
        value += increment;
        setValue((long) 0, value, 4);
        return value;
    }

}
