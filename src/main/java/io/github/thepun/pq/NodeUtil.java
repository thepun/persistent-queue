package io.github.thepun.pq;

// TODO: think about false sharing
// TODO: wrap element access to static method
final class NodeUtil {

    private static final int NODE_SIZE = 12;
    private static final int NODE_DATA_SIZE = 8;
    private static final int NEXT_FREE_NODE_INDEX = 8;
    private static final int NEXT_NODE_INDEX = 9;
    private static final int PREV_NODE_INDEX = 10;
    private static final int NODE_GENERATION_INDEX = 11;
    private static final int NODE_SIZE_WITHOUT_GENERATION = 11;
    private static final Object[] EMPTY_ARRAY = new Object[NODE_SIZE_WITHOUT_GENERATION];

    static final int NODE_DATA_SHIFT = 3;
    static final int NODE_DATA_SIZE_MASK = NODE_DATA_SIZE - 1;

    static Object[] createNewNode() {
        Generation generation = new Generation();
        Object[] node = new Object[NODE_SIZE];
        node[NODE_GENERATION_INDEX] = generation;
        return node;
    }

    static void setNextNode(Object[] node, Object[] nextNode) {
       node[NodeUtil.NEXT_NODE_INDEX] = nextNode;
    }

    static Object[] getNextNode(Object[] node) {
        return (Object[]) node[NodeUtil.NEXT_NODE_INDEX];
    }

    static void setPrevNode(Object[] node, Object[] prevNode) {
        node[NodeUtil.PREV_NODE_INDEX] = prevNode;
    }

    static Object[] getPrevNode(Object[] node) {
        return (Object[]) node[NodeUtil.PREV_NODE_INDEX];
    }

    static void setNextFreeNode(Object[] node, Object freeNode) {
        node[NodeUtil.NEXT_FREE_NODE_INDEX] = freeNode;
    }

    static Object[] getNextFreeNode(Object[] node) {
        return (Object[]) node[NodeUtil.NEXT_FREE_NODE_INDEX];
    }

    static void incrementGeneration(Object[] node) {
        ((Generation) node[NodeUtil.NODE_GENERATION_INDEX]).increment();
    }

    static int currentGeneration(Object[] node) {
        return ((Generation) node[NodeUtil.NODE_GENERATION_INDEX]).getValue();
    }

    static void clear(Object[] node) {
        System.arraycopy(EMPTY_ARRAY, 0, node, 0, NODE_SIZE_WITHOUT_GENERATION);
    }
}
