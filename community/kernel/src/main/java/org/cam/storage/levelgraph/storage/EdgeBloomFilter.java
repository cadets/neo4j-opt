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

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.cam.storage.levelgraph.datatypes.Edge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**

    Bloom filter to accelerate edge search in a super block.
    Deletions are not supported currently. For that, need to implement counting based bloom filter and a raw data filter which reuses the data stored by this class.


 */
public class EdgeBloomFilter {
    BloomFilter<Edge> bloomFilter;
    int maxCount;

    public EdgeBloomFilter(int numberOfAdditions) {
        maxCount = numberOfAdditions;
        bloomFilter = BloomFilter.create(EdgeFunnel.INSTANCE, numberOfAdditions, 0.03);
    }

    public EdgeBloomFilter(InputStream in, int numberOfAdditions) throws IOException {
        maxCount = numberOfAdditions;
        bloomFilter = BloomFilter.readFrom(in, EdgeFunnel.INSTANCE);
    }

    public EdgeBloomFilter(BloomFilter<Edge> filter, int numberOfAdditions) {
        maxCount = numberOfAdditions;
        bloomFilter = filter;
    }

    public void addElement(Edge edge) {
        bloomFilter.put(edge);
    }

    public boolean mightContain(Edge edge) {
        return bloomFilter.mightContain(edge);
    }

    public void writeTo(OutputStream out) throws IOException {
        bloomFilter.writeTo(out);
    }

    public EdgeBloomFilter mergeFilters(EdgeBloomFilter other) {
        BloomFilter<Edge> copy = bloomFilter.copy();
        copy.putAll(other.bloomFilter);
        return new EdgeBloomFilter(copy, maxCount * 2);
    }

    private enum EdgeFunnel implements Funnel<Edge> {
        INSTANCE;

        @Override
        public void funnel(Edge from, PrimitiveSink into) {
            into.putBytes(from.endsToBytes());
        }
    }


}
