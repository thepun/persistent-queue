package io.github.thepun.pq;

import sun.misc.Contended;

@Contended
final class SerializerCursor {

    private final TailCursor tailCursor;

    private long nextSequenceId;

    private long cursor;
    private long nodeIndex;
    private Object[] currentNode;

    SerializerCursor(TailCursor tailCursor) {
        this.tailCursor = tailCursor;
    }

    TailCursor getTailCursor() {
        return tailCursor;
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

    Object[] getCurrentNode() {
        return currentNode;
    }

    void setCurrentNode(Object[] currentNode) {
        this.currentNode = currentNode;
    }

    long getNextSequenceId() {
        return nextSequenceId;
    }

    void setNextSequenceId(long nextSequenceId) {
        this.nextSequenceId = nextSequenceId;
    }
}
