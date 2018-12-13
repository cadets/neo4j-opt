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
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.*;

public class LevelNode extends PropertyWrapper implements StorableData, Node {

    private Long internalId;

    private Long creatingTransaction;

    private Long deletingTransaction;

    //External Id is a temporary representation used for a string identifier for a levelNode.

    private Long externalId;

    public LevelNode() {
        externalId = (long) -1;
        internalId = (long) -1;
        creatingTransaction = (long) -1;
        deletingTransaction = (long) -1;
    }

    public LevelNode(long internalId) {
        this.internalId = internalId;
        externalId = (long) -1;
        creatingTransaction=(long)-1;
        deletingTransaction=(long)-1;
    }

    public LevelNode(byte[] bytes) {
        internalId = PrimitiveDeserialiser.getInstance().longFromByteArray(bytes, 1);
        externalId = PrimitiveDeserialiser.getInstance().longFromByteArray(bytes, 9); //No need to deserialise externalId, it should not be stored in the first place on disk.
    }
    @Override
    public byte[] toBytes() {
        byte[] results;
        byte[] id = PrimitiveSerialiser.getInstance().longToBytes(internalId);
        byte[] eid = PrimitiveSerialiser.getInstance().longToBytes(externalId);
        results = new byte[1 + id.length + eid.length];
        int index = 0;
        results[0] = (byte) 17;
        index += 1;
        System.arraycopy(id, 0, results, index, id.length);
        index += id.length;
        System.arraycopy(eid, 0, results, index, eid.length);
        return results;
    }

    public Long getExternalId() {
        return externalId;
    }

    public Long getCreatingTransaction() {
        return creatingTransaction;
    }

    public void setCreatingTransaction(Long creatingTransaction) {
        this.creatingTransaction = creatingTransaction;
    }

    public Long getDeletingTransaction() {
        return deletingTransaction;
    }

    public void setDeletingTransaction(Long deletingTransaction) {
        this.deletingTransaction = deletingTransaction;
    }

    public int hashCode(){
        return internalId.hashCode();
    }

    public Long getInternalId() {
        return internalId;
    }

    public void setInternalId(Long internalId) {
        this.internalId = internalId;
    }

    public boolean equals(Object other) {
        if (other instanceof LevelNode) {
            return internalId == ((LevelNode) other).internalId;
        }
        return false;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String toString(){
        return " "+externalId+" ";
    }


    @Override
    public long getId() {
        return getInternalId();
    }

    @Override
    public void delete() {

    }

    @Override
    public Iterable<Relationship> getRelationships() {
        return null;
    }

    @Override
    public boolean hasRelationship() {
        return false;
    }

    @Override
    public Iterable<Relationship> getRelationships(RelationshipType... relationshipTypes) {
        return null;
    }

    @Override
    public Iterable<Relationship> getRelationships(Direction direction, RelationshipType... relationshipTypes) {
        return null;
    }

    @Override
    public boolean hasRelationship(RelationshipType... relationshipTypes) {
        return false;
    }

    @Override
    public boolean hasRelationship(Direction direction, RelationshipType... relationshipTypes) {
        return false;
    }

    @Override
    public Iterable<Relationship> getRelationships(Direction direction) {
        return null;
    }

    @Override
    public boolean hasRelationship(Direction direction) {
        return false;
    }

    @Override
    public Iterable<Relationship> getRelationships(RelationshipType relationshipType, Direction direction) {
        return null;
    }

    @Override
    public boolean hasRelationship(RelationshipType relationshipType, Direction direction) {
        return false;
    }

    @Override
    public Relationship getSingleRelationship(RelationshipType relationshipType, Direction direction) {
        return null;
    }

    @Override
    public Relationship createRelationshipTo(Node node, RelationshipType relationshipType) {
        return null;
    }

    @Override
    public Iterable<RelationshipType> getRelationshipTypes() {
        return null;
    }

    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int getDegree(RelationshipType relationshipType) {
        return 0;
    }

    @Override
    public int getDegree(Direction direction) {
        return 0;
    }

    @Override
    public int getDegree(RelationshipType relationshipType, Direction direction) {
        return 0;
    }

    @Override
    public void addLabel(Label label) {

    }

    @Override
    public void removeLabel(Label label) {

    }

    @Override
    public Long getDataId() {
        return internalId;
    }

    @Override
    public boolean hasLabel(Label label) {
        return false;
    }

    @Override
    public Iterable<Label> getLabels() {
        return null;
    }
}
