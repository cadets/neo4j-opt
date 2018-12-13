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

public class PrimitiveDeserialiser {

    private static PrimitiveDeserialiser ourInstance = new PrimitiveDeserialiser();

    private PrimitiveDeserialiser() {
    }

    public static PrimitiveDeserialiser getInstance() {
        return ourInstance;
    }

    public long longFromByteArray(byte[] bytes) {
        return Longs.fromByteArray(bytes);
    }

    public long longFromByteArray(byte[] bytes, int index) {
        return longFromBytes(bytes[index], bytes[index + 1], bytes[index + 2], bytes[index + 3], bytes[index + 4], bytes[index + 5], bytes[index + 6], bytes[index + 7]);
    }

    public long longFromBytes(byte a, byte b, byte c, byte d, byte e, byte f, byte g, byte h) {
        return Longs.fromBytes(a, b, c, d, e, f, g, h);
    }


    public int intFromByteArray(byte[] bytes) {
        return Ints.fromByteArray(bytes);
    }

    public int intFromBytes(byte a, byte b, byte c, byte d) {
        return Ints.fromBytes(a, b, c, d);
    }

}
