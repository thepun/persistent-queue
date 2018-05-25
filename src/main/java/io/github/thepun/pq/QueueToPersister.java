package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;
import sun.misc.Contended;

import static io.github.thepun.pq.NodeUtil.*;

@Contended
final class QueueToPersister<T, C> implements PersistentQueueTail<T, C> {

    private final Head head;
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

    QueueToPersister(Callback<T, C> callback, Configuration configuration) {
        this.callback = callback;

        currentNode = createNode();
        localFreeNode = createNode();
        externalFreeNode = createNode();
        previousExternalFreeNode = createNode();
        currentExternalFreeNode = externalFreeNode;
        currentExternalFreeNode[NEXT_FREE_NODE_INDEX] = previousExternalFreeNode;

        head = new Head(this);

        MemoryFence.full();
    }

    @Override
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

    Head getHead() {
        return head;
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

    @Contended
    static final class Head {

        private final Callback<?, ?> callback;

        private long readIndex;
        private long nodeIndex;
        private Object[] freeNode;
        private Object[] currentNode;

        private Head(QueueToPersister<?, ?> tail) {
            callback = tail.callback;
        }

        int get(Object[] buffer, int offset, int length) {
            int count = 0;

            do {

                count++;
            } while (count < length);

            return count;
        }
    }
}
