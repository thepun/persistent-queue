package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;
import sun.misc.Contended;

import static io.github.thepun.pq.NodeUtil.*;

@Contended
public final class PersistentQueueTail<T, C> {

    private final Callback<T, C> callback;

    private long writeIndex;
    private long nodeIndex;
    private Object[] currentNode;
    private Object[] localFreeNode;

    private int currentExternalFreeNodeGen;
    private Object[] currentExternalFreeNode;

    private int previousExternalFreeNodeGen;
    private Object[] previousExternalFreeNode;

    @Contended
    private Object[] externalFreeNode;

    PersistentQueueTail(Configuration configuration) {
        callback = null;

        currentNode = createNode();
        localFreeNode = createNode();
        externalFreeNode = createNode();
        currentExternalFreeNode = externalFreeNode;
        previousExternalFreeNode = null;
        MemoryFence.full();
    }

    public void add(T element, C elementContext) {
        long writerIndexVar = writeIndex;
        long nodeIndexVar = nodeIndex;
        Object[] currentNodeVar = currentNode;

        long elementNodeIndex = writerIndexVar >> NODE_USER_SHIFT;
        if (elementNodeIndex != nodeIndexVar) {
            nodeIndex = elementNodeIndex;

            Object[] newNode = getFreeNode();
            currentNodeVar = newNode;
            currentNode = newNode;

            MemoryFence.store();
            currentNodeVar[NEXT_NODE_INDEX] = newNode;
        }

        writeIndex = writerIndexVar + 1;

        int elementIndex = (int) ((writerIndexVar & NODE_USER_SIZE_MASK) << 1);
        currentNodeVar[elementIndex | 1] = elementContext;
        MemoryFence.store();
        currentNodeVar[elementIndex] = element;
    }

    private Object[] getFreeNode() {
        Object[] localFreeNodeVar = localFreeNode;
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

        MemoryFence.load(); // ensure we do not load anything before we check local free nodes

        Object[] externalFreeNodeVar = externalFreeNode;

        // if external free node is still not changed we assume that there are not enough nodes and we have to create new
        int currentExternalFreeNodeGenVar = currentExternalFreeNodeGen;
        Object[] currentExternalFreeNodeVar = currentExternalFreeNode;
        int gen = ((Generation) externalFreeNodeVar[NODE_GENERATION_INDEX]).getValue();
        if (externalFreeNodeVar == currentExternalFreeNodeVar && gen == currentExternalFreeNodeGenVar) {
            return createNode();
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
}
