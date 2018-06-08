/**
 * Copyright (C)2011 - Marat Gariev <thepun599@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.thepun.pq;

final class HeadCursor extends HeadCursorMid {

    private long p0, p1, p2, p3, p4, p5, p6, p7;

    HeadCursor(SerializerCursor serializerCursor) {
        super(serializerCursor);
    }
}

class HeadCursorPre {

    private long p0, p1, p2, p3, p4, p5;

}

class HeadCursorMid extends HeadCursorPre {

    private final SerializerCursor serializerCursor;

    private Data data;
    private Sequence sequence;
    private long lastSequenceId;
    private long cursor;
    private long nodeIndex;
    private Object[] freeNode;
    private Object[] currentNode;

    HeadCursorMid(SerializerCursor serializerCursor) {
        this.serializerCursor = serializerCursor;
    }

    SerializerCursor getSerializerCursor() {
        return serializerCursor;
    }

    long getCursor() {
        return cursor;
    }

    void setCursor(long cursor) {
        this.cursor = cursor;
    }

    long getNodeIndex() {
        return nodeIndex;
    }

    void setNodeIndex(long nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    Object[] getFreeNode() {
        return freeNode;
    }

    void setFreeNode(Object[] freeNode) {
        this.freeNode = freeNode;
    }

    Object[] getCurrentNode() {
        return currentNode;
    }

    void setCurrentNode(Object[] currentNode) {
        this.currentNode = currentNode;
    }

    Data getData() {
        return data;
    }

    void setData(Data data) {
        this.data = data;
    }

    Sequence getSequence() {
        return sequence;
    }

    void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    long getLastSequenceId() {
        return lastSequenceId;
    }

    void setLastSequenceId(long lastSequenceId) {
        this.lastSequenceId = lastSequenceId;
    }
}