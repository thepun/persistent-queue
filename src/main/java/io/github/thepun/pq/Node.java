package io.github.thepun.pq;

import io.github.thepun.unsafe.ArrayMemory;
import io.github.thepun.unsafe.TypeSize;

final class Node {

    private static final int NODE_DATA_SIZE = 2 * 1024;
    private static final int NODE_SIZE = NODE_DATA_SIZE + 2 * 64 / TypeSize.REFERENCE_ + 2;
    private static final int NEXT_FREE_NODE_INDEX = 64 / TypeSize.REFERENCE_;
    private static final long NEXT_FREE_NODE_OFFSET = ArrayMemory.firstElementOffset() + NEXT_FREE_NODE_INDEX * TypeSize.REFERENCE_;
    private static final int NODE_DATA_INDEX = NEXT_FREE_NODE_INDEX + 1;
    private static final long NODE_DATA_OFFSET = ArrayMemory.firstElementOffset() + NODE_DATA_INDEX * TypeSize.REFERENCE_;
    private static final int NEXT_NODE_INDEX = NODE_DATA_INDEX + NODE_DATA_SIZE;
    private static final long NEXT_NODE_OFFSET = ArrayMemory.firstElementOffset() + NEXT_NODE_INDEX * TypeSize.REFERENCE_;
    private static final Object[] EMPTY_ARRAY = new Object[NODE_DATA_SIZE];


    static final int NODE_DATA_SHIFT = 11;
    static final int NODE_DATA_SIZE_MASK = NODE_DATA_SIZE - 1;

    static Object[] createNew() {
        return new Object[NODE_SIZE];
    }

    static Object getElement(Object[] node, int index) {
        return ArrayMemory.getObject(node, NODE_DATA_OFFSET + index * TypeSize.REFERENCE_);
    }

    static void setElement(Object[] node, int index, Object element) {
        ArrayMemory.setObject(node, NODE_DATA_OFFSET + index * TypeSize.REFERENCE_, element);
    }

    static Object[] getNext(Object[] node) {
        return (Object[]) ArrayMemory.getObject(node, NEXT_NODE_OFFSET);
    }

    static void setNext(Object[] node, Object[] nextNode) {
        ArrayMemory.setObject(node, NEXT_NODE_OFFSET, nextNode);
    }

    static Object[] getNextFree(Object[] node) {
        return (Object[]) ArrayMemory.getObject(node, NEXT_FREE_NODE_OFFSET);
    }

    static void setNextFree(Object[] node, Object freeNode) {
        ArrayMemory.setObject(node, NEXT_FREE_NODE_OFFSET, freeNode);
    }

    static void clear(Object[] node) {
        System.arraycopy(EMPTY_ARRAY, 0, node, NODE_DATA_INDEX, NODE_DATA_SIZE);
    }
}
