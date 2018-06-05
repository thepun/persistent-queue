package io.github.thepun.pq;

final class NodeUtil {

    private static final int NODE_SIZE = 18;
    private static final int NODE_DATA_SIZE = 16;
    private static final int NODE_GENERATION_INDEX = 16;
    private static final int NEXT_FREE_NODE_INDEX = 17;
    private static final int NEXT_NODE_INDEX = NODE_SIZE - 1;

    static final int NODE_DATA_SHIFT = 4;
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
}
