package io.github.thepun.pq;

import sun.misc.Contended;

@Contended
final class HeadCursor {

    private final TailCursor tailCursor;
    private final SerializerCursor serializerCursor;

    private Data data;
    private Commit commit;
    private Sequence sequence;
    private long sequenceId;

    private long cursor;
    private long nodeIndex;
    private Object[] freeNode;
    private Object[] currentNode;

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

    Data getData() {
        return data;
    }

    void setData(Data data) {
        this.data = data;
    }

    Commit getCommit() {
        return commit;
    }

    void setCommit(Commit commit) {
        this.commit = commit;
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
