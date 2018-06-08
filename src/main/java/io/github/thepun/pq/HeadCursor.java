package io.github.thepun.pq;

import sun.misc.Contended;

@Contended
final class HeadCursor {

    private final TailCursor tailCursor;
    private final SerializerCursor serializerCursor;

    private Data data;
    private Sequence sequence;
    private long lastSequenceId;

    private long cursor;
    private long nodeIndex;
    private Object[] freeNode;
    private Object[] currentNode;
    //private Object[] nextNodeToFree;

    HeadCursor(SerializerCursor serializerCursor) {
        this.serializerCursor = serializerCursor;

        tailCursor = serializerCursor.getTailCursor();
    }

    TailCursor getTailCursor() {
        return tailCursor;
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

    /*Object[] getNextNodeToFree() {
        return nextNodeToFree;
    }

    void setNextNodeToFree(Object[] nextNodeToFree) {
        this.nextNodeToFree = nextNodeToFree;
    }*/

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
