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

import org.cam.storage.levelgraph.dataUtils.PrimitiveDeserialiser;
import org.cam.storage.levelgraph.dataUtils.PrimitiveSerialiser;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class Edge extends PropertyWrapper implements StorableData, Relationship {

    private long neighbour, edgeid, node;
    private Direction direction;
    private PropertyEntity props;
    private long creatingTransaction;
    private long deletingTransaction;

    public Edge(long from, long neighbour, long edgeid, Character direction, PropertyEntity properties) {
        node = from;
        this.neighbour = neighbour;
        this.edgeid = edgeid;
        this.direction = new Direction(direction);
        this.props = properties;
    }

    public Edge(long from, long neighbour, long edgeid) {
        node = from;
        this.neighbour = neighbour;
        this.edgeid = edgeid;
        this.direction = new Direction(Direction.DirectionValues.Bidirectional);
        this.props = null;
    }
    public Edge(long from, long neighbour, long edgeid, Direction direction, PropertyEntity properties) {
        node = from;
        this.neighbour = neighbour;
        this.edgeid = edgeid;
        this.direction = direction;
        this.props = properties;
    }

    public Edge(long neighbour, long edgeid, Character direction, PropertyEntity properties) {
        this.neighbour = neighbour;
        this.edgeid = edgeid;
        this.direction = new Direction(direction);
        this.props = properties;
    }

    public Edge(long neighbour, long edgeid, Direction direction, PropertyEntity properties) {
        this.neighbour = neighbour;
        this.edgeid = edgeid;
        this.direction = direction;
        this.props = properties;
    }

    public Edge(byte[] bytes) {
        int index = 1;
        neighbour = getLong(bytes, index);
        index = 9;
        node = getLong(bytes, index);
        index += 8;
        edgeid = getLong(bytes, index);
        index += 8;
        direction = new Direction((char) bytes[index]);
        creatingTransaction = -1;
        deletingTransaction = -1;
    }

    private long getLong(byte[] bytes, int index) {
        return PrimitiveDeserialiser.getInstance().longFromBytes(bytes[index], bytes[index + 1], bytes[index + 2], bytes[index + 3], bytes[index + 4], bytes[index + 5], bytes[index + 6], bytes[index + 7]);
    }

    public byte[] endsToBytes() {
        byte[] results;
        byte[] ngb = PrimitiveSerialiser.getInstance().longToBytes(neighbour);
        byte[] nodeb = PrimitiveSerialiser.getInstance().longToBytes(node);
        results = new byte[ngb.length + nodeb.length];
        int index = 0;
        System.arraycopy(ngb, 0, results, index, ngb.length);
        index += ngb.length;
        System.arraycopy(nodeb, 0, results, index, nodeb.length);
        return results;
    }

    @Override
    public byte[] toBytes() {
        byte[] results;
        byte length = (byte) 26;//PrimitiveSerialiser.getInstance().intToBytes(26);
        byte[] ngb = PrimitiveSerialiser.getInstance().longToBytes(neighbour);
        byte[] nodeb = PrimitiveSerialiser.getInstance().longToBytes(node);
        byte[] eid = PrimitiveSerialiser.getInstance().longToBytes(edgeid);

        /*
        toBytes converts an edge into a writable entity. Creating and deleting transactions are hence not needed in the storage layer.
         */

        results = new byte[ngb.length + nodeb.length + eid.length + 2];
        int index = 0;
        results[0] = length;
        index += 1;
        System.arraycopy(ngb, 0, results, index, ngb.length);
        index += ngb.length;
        System.arraycopy(nodeb, 0, results, index, nodeb.length);
        index += nodeb.length;
        System.arraycopy(eid, 0, results, index, eid.length);
        index += eid.length;
        results[index] = direction.toByte();
        return results;
    }

    public long getDeletingTransaction() {
        return deletingTransaction;
    }

    public void setDeletingTransaction(long deletingTransaction) {
        this.deletingTransaction = deletingTransaction;
    }

    public long getCreatingTransaction() {
        return creatingTransaction;
    }

    public void setCreatingTransaction(long creatingTransaction) {
        this.creatingTransaction = creatingTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;
        if (edge.edgeid == edgeid)
            return true;
        if (edge.node != node) return false;
        return neighbour == edge.neighbour;
//        return direction.equals(edge.direction);
    }

    @Override
    public int hashCode() {
        int result = (int) (neighbour ^ (neighbour >>> 32));
        result = 31 * result + (int) (edgeid ^ (edgeid >>> 32));
        result = 31 * result + (int) (node ^ (node >>> 32));
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + (props != null ? props.hashCode() : 0);
        result = 31 * result + (int) (creatingTransaction ^ (creatingTransaction >>> 32));
        result = 31 * result + (int) (deletingTransaction ^ (deletingTransaction >>> 32));
        return result;
    }

    public boolean equals(long to) {
        return neighbour == to;
    }

    public String toString() {
        return "(" + edgeid + ", " + neighbour + direction + ")";
    }

    public PropertyEntity getProps() {
        return props;
    }

    public void setProps(PropertyEntity props) {
        this.props = props;
    }

    public Direction getDirection() {

        return direction;
    }

    public void setDirection(Character direction) {
        this.direction = new Direction(direction);
    }

    public long getEdgeid() {
        return edgeid;
    }

    public void setEdgeid(long edgeid) {
        this.edgeid = edgeid;
    }

    public long getNeighbour() {
        return neighbour;
    }

    public void setNeighbour(long neighbour) {
        this.neighbour = neighbour;
    }

    public long getSourceNode() {
        return node;
    }

    public Long getLongId() {
        return edgeid;
    }

    @Override
    public long getId() {
        return edgeid;
    }

    @Override
    public void delete() {

    }

    @Override
    public Node getStartNode() {
        return null;
    }

    @Override
    public Node getEndNode() {
        return null;
    }

    @Override
    public Node getOtherNode(Node node) {
        return null;
    }

    @Override
    public Node[] getNodes() {
        return new Node[0];
    }

    @Override
    public RelationshipType getType() {
        return null;
    }

    @Override
    public boolean isType(RelationshipType relationshipType) {
        return false;
    }

    @Override
    public Long getDataId() {
        return edgeid;
    }
}
