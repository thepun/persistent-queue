package io.github.thepun.pq;

final class NodeUtil {

    static final int NODE_SIZE = 18;
    static final int NODE_USER_SHIFT = 3;
    static final int NODE_USER_SIZE = 8;
    static final int NODE_USER_SIZE_MASK = NODE_USER_SIZE - 1;
    static final int NODE_USER_FIRST_INDEX = 2;
    static final int NODE_USER_LAST_INDEX = NODE_USER_SIZE + NODE_USER_FIRST_INDEX - 1;
    static final int NEXT_NODE_INDEX = NODE_SIZE - 1;
    static final int NEXT_FREE_NODE_INDEX = 1;
    static final int NODE_GENERATION_INDEX = 0;

    static Object[] createNode() {
        Generation generation = new Generation();
        Object[] node = new Object[NODE_SIZE];
        node[NODE_GENERATION_INDEX] = generation;
        return node;
    }
}
