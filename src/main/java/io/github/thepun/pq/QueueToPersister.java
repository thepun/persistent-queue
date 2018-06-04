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
            Object[] newNode = getFreeNode(tailCursorVar);
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

    private static Object[] getFreeNode(TailCursor tailCursor) {
        Object[] localFreeNodeVar = tailCursor.getLocalFreeNode();

        // we dont have any local node to use
        if (localFreeNodeVar != null) {
            Object[] nextNode = (Object[]) localFreeNodeVar[NodeUtil.NEXT_FREE_NODE_INDEX];
            if (nextNode != null) {
                // check if we found previous external node with the same generation
                Object[] previousExternalFreeNodeVar = tailCursor.getPreviousExternalFreeNode();
                if (nextNode == previousExternalFreeNodeVar) {
                    int gen = ((Generation) previousExternalFreeNodeVar[NodeUtil.NODE_GENERATION_INDEX]).getValue();
                    if (gen == tailCursor.getPreviousExternalFreeNodeGen()) {
                        nextNode = null;
                    }
                }
            }

            localFreeNodeVar[NodeUtil.NEXT_FREE_NODE_INDEX] = null;
            tailCursor.setLocalFreeNode(nextNode);
            return localFreeNodeVar;
        }

        // ensure we do not load anything before we check local free nodes
        MemoryFence.load();

        // externalFreeNode will be accessed from another thread so we load it only once
        Object[] externalFreeNodeVar = tailCursor.getExternalFreeNode();
        MemoryFence.load();

        // if external free node is still not changed we assume that there are not enough nodes and we have to create new
        int currentExternalFreeNodeGenVar = tailCursor.getCurrentExternalFreeNodeGen();
        Object[] currentExternalFreeNodeVar = tailCursor.getCurrentExternalFreeNode();
        int gen = ((Generation) externalFreeNodeVar[NodeUtil.NODE_GENERATION_INDEX]).getValue();
        if (externalFreeNodeVar == currentExternalFreeNodeVar && gen == currentExternalFreeNodeGenVar) {
            return NodeUtil.createNewNode();
        }

        // save external nodes
        tailCursor.setPreviousExternalFreeNodeGen(currentExternalFreeNodeGenVar);
        tailCursor.setPreviousExternalFreeNode(currentExternalFreeNodeVar);
        tailCursor.setCurrentExternalFreeNodeGen(gen);
        tailCursor.setCurrentExternalFreeNode(externalFreeNodeVar);

        Object[] nextNode = (Object[]) externalFreeNodeVar[NodeUtil.NEXT_FREE_NODE_INDEX];
        externalFreeNodeVar[NodeUtil.NEXT_FREE_NODE_INDEX] = null;
        tailCursor.setLocalFreeNode(nextNode);
        return externalFreeNodeVar;
    }
}
