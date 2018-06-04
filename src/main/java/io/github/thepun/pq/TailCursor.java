package io.github.thepun.pq;

import sun.misc.Contended;

@Contended
final class TailCursor {

    private long cursor;
    private long nodeIndex;
    private Object[] currentNode;
    private Object[] localFreeNode;

    private int currentExternalFreeNodeGen;
    private Object[] currentExternalFreeNode;

    private int previousExternalFreeNodeGen;
    private Object[] previousExternalFreeNode;

    @Contended("external")
    private Object[] externalFreeNode;

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

    Object[] getLocalFreeNode() {
        return localFreeNode;
    }

    void setLocalFreeNode(Object[] localFreeNode) {
        this.localFreeNode = localFreeNode;
    }

    int getCurrentExternalFreeNodeGen() {
        return currentExternalFreeNodeGen;
    }

    void setCurrentExternalFreeNodeGen(int currentExternalFreeNodeGen) {
        this.currentExternalFreeNodeGen = currentExternalFreeNodeGen;
    }

    Object[] getCurrentExternalFreeNode() {
        return currentExternalFreeNode;
    }

    void setCurrentExternalFreeNode(Object[] currentExternalFreeNode) {
        this.currentExternalFreeNode = currentExternalFreeNode;
    }

    int getPreviousExternalFreeNodeGen() {
        return previousExternalFreeNodeGen;
    }

    void setPreviousExternalFreeNodeGen(int previousExternalFreeNodeGen) {
        this.previousExternalFreeNodeGen = previousExternalFreeNodeGen;
    }

    Object[] getPreviousExternalFreeNode() {
        return previousExternalFreeNode;
    }

    void setPreviousExternalFreeNode(Object[] previousExternalFreeNode) {
        this.previousExternalFreeNode = previousExternalFreeNode;
    }

    Object[] getExternalFreeNode() {
        return externalFreeNode;
    }

    void setExternalFreeNode(Object[] externalFreeNode) {
        this.externalFreeNode = externalFreeNode;
    }
}
