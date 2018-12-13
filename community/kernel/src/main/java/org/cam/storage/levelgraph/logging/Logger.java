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

package org.cam.storage.levelgraph.logging;

import org.cam.storage.levelgraph.transaction.TransactionInterface;
import org.cam.storage.levelgraph.transaction.TransactionUpdatesCache;

import java.util.ArrayList;

public interface Logger {

    void addUpdate(TransactionUpdatesCache updates, TransactionInterface transaction);

    ArrayList<TransactionUpdatesCache.Updates> getUncommittedUpdates();

    void removeTransactionUpdates(TransactionInterface transaction);

}
