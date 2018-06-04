package io.github.thepun.pq;

import sun.misc.Contended;

@Contended
final class SerializerCursor {

    private final TailCursor tailCursor;

    private Data data;
    private Sequence sequence;
    private long sequenceId;

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

    long getSequenceId() {
        return sequenceId;
    }

    void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }
}
