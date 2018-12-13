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

package org.cam.storage.levelgraph.dataUtils;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class PrimitiveSerialiser {

    private static PrimitiveSerialiser ourInstance = new PrimitiveSerialiser();

    private PrimitiveSerialiser() {
    }

    public static PrimitiveSerialiser getInstance() {
        return ourInstance;
    }

    public byte[] longToBytes(long x) {
        return Longs.toByteArray(x);
    }

    public byte[] intToBytes(int x) {
        return Ints.toByteArray(x);
    }

    public long byteArrayToLong(byte[] bytes) {
        return Longs.fromByteArray(bytes);
    }

    public int byteArrayToInt(byte[] bytes) {
        return Ints.fromByteArray(bytes);
    }


}
