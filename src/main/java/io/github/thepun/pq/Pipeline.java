package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;
import sun.misc.Contended;

@Contended
final class Pipeline implements PersistentQueueTail<Object, Object> {

    private static final int NODE_SIZE = 18;
    private static final int NEXT_NODE_INDEX = NODE_SIZE - 1;
    private static final int NODE_DATA_SHIFT = 4;
    private static final int NODE_DATA_SIZE = 16;
    private static final int NODE_DATA_SIZE_MASK = NODE_DATA_SIZE - 1;
    private static final int NEXT_FREE_NODE_INDEX = 1;
    private static final int NODE_GENERATION_INDEX = 0;


    private final Head head;

    private long writeIndex;
    private long nodeIndex;
    private Object[] currentNode;
    private Object[] localFreeNode;

    private int currentExternalFreeNodeGen;
    private Object[] currentExternalFreeNode;

    private int previousExternalFreeNodeGen;
    private Object[] previousExternalFreeNode;

    @Contended("external")
    private Object[] externalFreeNode;

    Pipeline(Configuration<Object, Object> configuration) {
        // TODO: implement multiple free nodes to be initialized on start not just one

        currentNode = createNewNode();
        localFreeNode = createNewNode();
        externalFreeNode = createNewNode();
        previousExternalFreeNode = createNewNode();
        currentExternalFreeNode = externalFreeNode;
        currentExternalFreeNode[NEXT_FREE_NODE_INDEX] = previousExternalFreeNode;

        head = new Head(this);

        MemoryFence.full();
    }

    @Override
    public void add(Object element, Object elementContext) {
        long writerIndexVar = writeIndex;
        long nodeIndexVar = nodeIndex;
        Object[] currentNodeVar = currentNode;

        long elementNodeIndex = writerIndexVar >> NODE_DATA_SHIFT;
        if (elementNodeIndex != nodeIndexVar) {
            // remember node index
            nodeIndex = elementNodeIndex;

            // get new node
            Object[] newNode = getFreeNode();
            currentNodeVar = newNode;
            currentNode = newNode;

            // reassure we do not expose new node before it is ready
            MemoryFence.store();

            // attach new node to chain
            currentNodeVar[NEXT_NODE_INDEX] = newNode;
        }

        writeIndex = writerIndexVar + 2;

        int elementIndex = (int) (writerIndexVar & NODE_DATA_SIZE_MASK);
        currentNodeVar[elementIndex | 1] = elementContext;
        MemoryFence.store();
        currentNodeVar[elementIndex] = element;
    }

    Head getHead() {
        return head;
    }

    private Object[] getFreeNode() {
        Object[] localFreeNodeVar = localFreeNode;

        // we dont have any local node to use
        if (localFreeNodeVar != null) {
            Object[] nextNode = (Object[]) localFreeNodeVar[NEXT_FREE_NODE_INDEX];
            if (nextNode != null) {
                // check if we found previous external node with the same generation
                Object[] previousExternalFreeNodeVar = previousExternalFreeNode;
                if (nextNode == previousExternalFreeNodeVar) {
                    int gen = ((Generation) previousExternalFreeNodeVar[NODE_GENERATION_INDEX]).getValue();
                    if (gen == previousExternalFreeNodeGen) {
                        nextNode = null;
                    }
                }
            }

            localFreeNodeVar[NEXT_FREE_NODE_INDEX] = null;
            localFreeNode = nextNode;
            return localFreeNodeVar;
        }

        // ensure we do not load anything before we check local free nodes
        MemoryFence.load();

        // externalFreeNode will be accessed from another thread so we load it only once
        Object[] externalFreeNodeVar = externalFreeNode;

        // if external free node is still not changed we assume that there are not enough nodes and we have to create new
        int currentExternalFreeNodeGenVar = currentExternalFreeNodeGen;
        Object[] currentExternalFreeNodeVar = currentExternalFreeNode;
        int gen = ((Generation) externalFreeNodeVar[NODE_GENERATION_INDEX]).getValue();
        if (externalFreeNodeVar == currentExternalFreeNodeVar && gen == currentExternalFreeNodeGenVar) {
            return createNewNode();
        }

        // save external nodes
        previousExternalFreeNodeGen = currentExternalFreeNodeGenVar;
        previousExternalFreeNode = currentExternalFreeNodeVar;
        currentExternalFreeNodeGen = gen;
        currentExternalFreeNode = externalFreeNodeVar;

        Object[] nextNode = (Object[]) externalFreeNodeVar[NEXT_FREE_NODE_INDEX];
        externalFreeNodeVar[NEXT_FREE_NODE_INDEX] = null;
        localFreeNode = nextNode;
        return externalFreeNodeVar;
    }

    private static Object[] createNewNode() {
        Generation generation = new Generation();
        Object[] node = new Object[NODE_SIZE];
        node[NODE_GENERATION_INDEX] = generation;
        return node;
    }


}
