package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;

final class QueueToPersister implements PersistentQueueTail<Object, Object> {

    private final TailCursor tailCursor;

    QueueToPersister(TailCursor tailCursor) {
        this.tailCursor = tailCursor;
    }

    @Override
    public void add(Object element, Object elementContext) {
        TailCursor tailCursorVar = tailCursor;

        long writerIndexVar = tailCursorVar.getCursor();
        long nodeIndexVar = tailCursorVar.getNodeIndex();
        Object[] currentNodeVar = tailCursorVar.getCurrentNode();

        long elementNodeIndex = writerIndexVar >> NodeUtil.NODE_DATA_SHIFT;
        if (elementNodeIndex != nodeIndexVar) {
            // get new node
            Object[] newNode = getFreeNode();
            currentNodeVar = newNode;
            tailCursorVar.setNodeIndex(elementNodeIndex);
            tailCursorVar.setCurrentNode(newNode);

            // reassure we do not expose new node before it is ready
            MemoryFence.store();

            // attach new node to chain
            currentNodeVar[NodeUtil.NEXT_NODE_INDEX] = newNode;
        }

        tailCursorVar.setCursor(writerIndexVar + 2);

        int elementIndex = (int) (writerIndexVar & NodeUtil.NODE_DATA_SIZE_MASK);
        currentNodeVar[elementIndex | 1] = elementContext;
        MemoryFence.store();
        currentNodeVar[elementIndex] = element;
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
}
