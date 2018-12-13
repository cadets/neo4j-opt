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

public class Direction {
    byte toByte() {
        return (byte) getDirection().charValue();
    }

    private DirectionValues direction;

    public Character getDirection() {
        switch (direction){
            case Incoming:
                return '0';
            case Outgoing:
                return '1';
            case Bidirectional:
                return '2';
        }
        return '2';
    }

    public enum DirectionValues {
        Incoming, Outgoing, Bidirectional
    }

    public DirectionValues getDirectionValue(){
        return direction;
    }

    public void setDirection(DirectionValues direction) {
        this.direction = direction;
    }

    public Direction(Character direction){
        switch (direction){
            case '0':
                setDirection(DirectionValues.Incoming);
            case '1':
                setDirection(DirectionValues.Outgoing);
            case '2':
                setDirection(DirectionValues.Bidirectional);
            default:
                setDirection(DirectionValues.Bidirectional);
        }
    }

    public Direction(DirectionValues directionValue) {
        direction=directionValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Direction direction1 = (Direction) o;

        return direction == direction1.direction;
    }

    @Override
    public int hashCode() {
        return direction != null ? direction.hashCode() : 0;
    }
}
