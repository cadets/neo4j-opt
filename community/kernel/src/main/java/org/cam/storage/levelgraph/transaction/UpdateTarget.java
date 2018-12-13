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

package org.cam.storage.levelgraph.transaction;


public enum UpdateTarget {
    NodeUpdate, EdgeUpdate, EdgeTypeUpdate, NodeLabelUpdate,
    NodePropertyUpdate,
    EdgeLabelUpdate,
    EdgePropertyUpdate;

    public static UpdateTarget fromByte(byte value) {
        switch (value) {
            case 0:
                return NodeUpdate;
            case 1:
                return EdgeUpdate;
            case 2:
                return EdgeTypeUpdate;
            case 3:
                return NodeLabelUpdate;
            case 4:
                return NodePropertyUpdate;
            case 5:
                return EdgeLabelUpdate;
            case 6:
                return EdgePropertyUpdate;
            case 11:
                return NodeUpdate;
        }
        return NodeUpdate;
    }

    byte toByte(UpdateTarget updateTarget) {
        switch (updateTarget) {
            case NodeUpdate:
                return 0;
            case EdgeUpdate:
                return 1;
            case EdgeTypeUpdate:
                return 2;
            case NodeLabelUpdate:
                return 3;
            case NodePropertyUpdate:
                return 4;
            case EdgeLabelUpdate:
                return 5;
            case EdgePropertyUpdate:
                return 6;
            default:
                return 11;
        }
    }
}
