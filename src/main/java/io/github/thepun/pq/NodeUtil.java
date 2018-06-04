package io.github.thepun.pq;

final class NodeUtil {

    private static final int NODE_SIZE = 18;
    static final int NEXT_NODE_INDEX = NODE_SIZE - 1;
    static final int NODE_DATA_SHIFT = 4;
    private static final int NODE_DATA_SIZE = 16;
    static final int NODE_DATA_SIZE_MASK = NODE_DATA_SIZE - 1;
    static final int NEXT_FREE_NODE_INDEX = 1;
    static final int NODE_GENERATION_INDEX = 0;

    static Object[] createNewNode() {
        Generation generation = new Generation();
        Object[] node = new Object[NODE_SIZE];
        node[NODE_GENERATION_INDEX] = generation;
        return node;
    }
}
