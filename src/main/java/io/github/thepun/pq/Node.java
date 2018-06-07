package io.github.thepun.pq;

import io.github.thepun.unsafe.ArrayMemory;
import io.github.thepun.unsafe.TypeSize;

final class Node {

    public static int COUNTER = 0;


    /*private static final int NODE_DATA_SIZE = 2 * 1024;
    private static final int NODE_SIZE = NODE_DATA_SIZE + 2 * 64 / TypeSize.REFERENCE_ + 6;
    private static final int NEXT_FREE_NODE_INDEX = 64 / TypeSize.REFERENCE_;
    private static final long NEXT_FREE_NODE_OFFSET = ArrayMemory.firstElementOffset() + NEXT_FREE_NODE_INDEX * TypeSize.REFERENCE_;
    private static final int NODE_GENERATION_INDEX = NEXT_FREE_NODE_INDEX + 2;
    private static final long NODE_GENERATION_OFFSET = ArrayMemory.firstElementOffset() + NODE_GENERATION_INDEX * TypeSize.REFERENCE_;
    private static final int NODE_DATA_INDEX = NODE_GENERATION_INDEX + 2;
    private static final long NODE_DATA_OFFSET = ArrayMemory.firstElementOffset() + NODE_DATA_INDEX * TypeSize.REFERENCE_;
    private static final int NEXT_NODE_INDEX = NODE_DATA_INDEX + NODE_DATA_SIZE;
    private static final long NEXT_NODE_OFFSET = ArrayMemory.firstElementOffset() + NEXT_NODE_INDEX * TypeSize.REFERENCE_;
    private static final Object[] EMPTY_ARRAY = new Object[NODE_DATA_SIZE];*/

    private static final int NODE_DATA_SIZE = 8;
    private static final int NODE_SIZE = 12;
    private static final int NEXT_FREE_NODE_INDEX = 11;
    private static final long NEXT_FREE_NODE_OFFSET = ArrayMemory.firstElementOffset() + NEXT_FREE_NODE_INDEX * TypeSize.REFERENCE_;
    private static final int NODE_GENERATION_INDEX = 10;
    private static final long NODE_GENERATION_OFFSET = ArrayMemory.firstElementOffset() + NODE_GENERATION_INDEX * TypeSize.REFERENCE_;
    private static final int NODE_DATA_INDEX = 0;
    private static final long NODE_DATA_OFFSET = ArrayMemory.firstElementOffset() + NODE_DATA_INDEX * TypeSize.REFERENCE_;
    private static final int NEXT_NODE_INDEX = 9;
    private static final long NEXT_NODE_OFFSET = ArrayMemory.firstElementOffset() + NEXT_NODE_INDEX * TypeSize.REFERENCE_;
    private static final Object[] EMPTY_ARRAY = new Object[NODE_DATA_SIZE];


    static final int NODE_DATA_SHIFT = 3;
    //static final int NODE_DATA_SHIFT = 11;
    static final int NODE_DATA_SIZE_MASK = NODE_DATA_SIZE - 1;

    static Object[] createNew() {
        COUNTER++;
        Generation generation = new Generation();
        Object[] node = new Object[NODE_SIZE];
        node[NODE_GENERATION_INDEX] = generation;
        node[8] = COUNTER;
        return node;
    }

    static Object getElement(Object[] node, int index) {
        return ArrayMemory.getObject(node, NODE_DATA_OFFSET + index * TypeSize.REFERENCE_);
        //return node[index];
    }

    static void setElement(Object[] node, int index, Object element) {
        if (element != null && (Integer) element == 39968) {
            Object o = null;
        }

        ArrayMemory.setObject(node, NODE_DATA_OFFSET + index * TypeSize.REFERENCE_, element);
        //node[index] = element;
    }

    static Object[] getNext(Object[] node) {
        return (Object[]) ArrayMemory.getObject(node, NEXT_NODE_OFFSET);
        //return (Object[]) node[Node.NEXT_NODE_INDEX];
    }

    static void setNext(Object[] node, Object[] nextNode) {
        ArrayMemory.setObject(node, NEXT_NODE_OFFSET, nextNode);
       //node[Node.NEXT_NODE_INDEX] = nextNode;
    }

    /*static void setPrev(Object[] node, Object[] prevNode) {
        node[Node.PREV_NODE_INDEX] = prevNode;
    }

    static Object[] getPrev(Object[] node) {
        return (Object[]) node[Node.PREV_NODE_INDEX];
    }*/

    static Object[] getNextFree(Object[] node) {
        return (Object[]) ArrayMemory.getObject(node, NEXT_FREE_NODE_OFFSET);
        //return (Object[]) node[Node.NEXT_FREE_NODE_INDEX];
    }

    static void setNextFree(Object[] node, Object freeNode) {
        ArrayMemory.setObject(node, NEXT_FREE_NODE_OFFSET, freeNode);
        //node[Node.NEXT_FREE_NODE_INDEX] = freeNode;
    }

    static int currentGeneration(Object[] node) {
        Generation generation = (Generation) ArrayMemory.getObject(node, NODE_GENERATION_OFFSET);
        return generation.getValue();
        //return ((Generation) node[Node.NODE_GENERATION_INDEX]).getValue();
    }

    static void clear(Object[] node) {
        System.arraycopy(EMPTY_ARRAY, 0, node, NODE_DATA_INDEX, NODE_DATA_SIZE);
        Generation generation = (Generation) ArrayMemory.getObject(node, NODE_GENERATION_OFFSET);
        generation.increment();
        //((Generation) node[Node.NODE_GENERATION_INDEX]).increment();
    }
}
