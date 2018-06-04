package io.github.thepun.pq;

import sun.misc.Contended;

@Contended
final class SerializerCursor {

    private final TailCursor tailCursor;

    private long cursor;
    private long nodeIndex;
    private Object[] freeNode;
    private Object[] currentNode;

    private Head(TailCursor tailCursor) {
        this.tailCursor = tailCursor;

        currentNode = tailCursor.getCurrentNode();
        freeNode = tailCursor.getExternalFreeNode();
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

    /*int get(Object[] buffer, int offset, int length) {
        Object[] currentNodeVar = currentNode;
        long readIndexVar = cursor;
        long nodeIndexVar = nodeIndex;

        int count = 0;
        int bufferIndex = offset;
        do {
            // check we need to move to another node
            long elementNodeIndex = readIndexVar >> NODE_DATA_SHIFT;
            if (elementNodeIndex != nodeIndexVar) {
                // try get next node from chain
                Object[] nextNode = (Object[]) currentNodeVar[NEXT_NODE_INDEX];
                if (nextNode == null) {
                    cursor = readIndexVar;
                    return count;
                }

                // free processed node
                currentNodeVar[NEXT_FREE_NODE_INDEX] = freeNode;
                ((Generation) currentNodeVar[NODE_GENERATION_INDEX]).increment();

                // ensure we expose free node only after it is prepared
                MemoryFence.store();

                // expose new free node
                pipeline.externalFreeNode = currentNodeVar;

                // use new node as current
                currentNodeVar = nextNode;
                nodeIndex = elementNodeIndex;
                currentNode = currentNodeVar;
            }

            int elementIndex = (int) (readIndexVar & NODE_DATA_SIZE_MASK);
            Object element = currentNodeVar[elementIndex];
            if (element == null) {
                // another thread didn't write to the index yet
                cursor = readIndexVar;
                return count;
            }

            buffer[bufferIndex] = element;
            buffer[bufferIndex | 1] = currentNodeVar[elementIndex | 1];

            // counters for next step
            readIndexVar += 2;
            bufferIndex += 2;
            count += 1;
        } while (count < length);

        cursor = readIndexVar;
        return count;
    }*/
}
