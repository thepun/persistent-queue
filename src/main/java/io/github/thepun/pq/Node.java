package io.github.thepun.pq;

// TODO: think about false sharing
// TODO: use native array access
final class Node {

    public static int COUNTER = 0;


    private static final int NODE_SIZE = 12;
    private static final int NODE_DATA_SIZE = 8;
    private static final int NEXT_FREE_NODE_INDEX = 8;
    private static final int NEXT_NODE_INDEX = 9;
    private static final int PREV_NODE_INDEX = 10;
    private static final int NODE_GENERATION_INDEX = 11;
    private static final Object[] EMPTY_ARRAY = new Object[NODE_DATA_SIZE];

    static final int NODE_DATA_SHIFT = 3;
    static final int NODE_DATA_SIZE_MASK = NODE_DATA_SIZE - 1;

    static Object[] createNew() {
        COUNTER++;
        Generation generation = new Generation();
        Object[] node = new Object[NODE_SIZE];
        node[NODE_GENERATION_INDEX] = generation;
        return node;
    }

    static Object getElement(Object[] node, int index) {
        return node[index];
    }

    static void setElement(Object[] node, int index, Object element) {
        node[index] = element;
    }

    static void setNext(Object[] node, Object[] nextNode) {
       node[Node.NEXT_NODE_INDEX] = nextNode;
    }

    static Object[] getNext(Object[] node) {
        return (Object[]) node[Node.NEXT_NODE_INDEX];
    }

    static void setPrevNode(Object[] node, Object[] prevNode) {
        node[Node.PREV_NODE_INDEX] = prevNode;
    }

    static Object[] getPrev(Object[] node) {
        return (Object[]) node[Node.PREV_NODE_INDEX];
    }

    static void setNextFree(Object[] node, Object freeNode) {
        node[Node.NEXT_FREE_NODE_INDEX] = freeNode;
    }

    static Object[] getNextFree(Object[] node) {
        return (Object[]) node[Node.NEXT_FREE_NODE_INDEX];
    }

    static int currentGeneration(Object[] node) {
        return ((Generation) node[Node.NODE_GENERATION_INDEX]).getValue();
    }

    static void clear(Object[] node) {
        System.arraycopy(EMPTY_ARRAY, 0, node, 0, NODE_DATA_SIZE);
        ((Generation) node[Node.NODE_GENERATION_INDEX]).increment();
    }
}
