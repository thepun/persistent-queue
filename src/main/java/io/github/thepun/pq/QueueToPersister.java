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

        if (element != null && (Integer) element == 39) {
            Object o = null;
        }

        long elementNodeIndex = writerIndexVar >> Node.NODE_DATA_SHIFT;
        if (elementNodeIndex != nodeIndexVar) {
            // get new node
            Object[] newNode = getFreeNode(tailCursorVar);

            if ((Integer) newNode[8] == 3) {
                Object o = null;
            }

            tailCursorVar.setNodeIndex(elementNodeIndex);
            tailCursorVar.setCurrentNode(newNode);
            Node.setNext(newNode, null);
            //Node.setPrev(newNode, currentNodeVar);

            // reassure we do not expose new node before it is ready
            MemoryFence.store();

            // attach new node to chain
            Node.setNext(currentNodeVar, newNode);
            currentNodeVar = newNode;
        }

        tailCursorVar.setCursor(writerIndexVar + 2);

        int elementIndex = (int) (writerIndexVar & Node.NODE_DATA_SIZE_MASK);
        Object prev = Node.getElement(currentNodeVar, elementIndex);
        if (prev != null) {
            Object o = null;
        }

        Node.setElement(currentNodeVar, elementIndex | 1, elementContext);
        MemoryFence.store();
        Node.setElement(currentNodeVar, elementIndex, element);
    }

    private static Object[] getFreeNode(TailCursor tailCursor) {
        Object[] localFreeNodeVar = tailCursor.getLocalFreeNode();

        // we dont have any local node to use
        if (localFreeNodeVar != null) {
            if ((Integer) localFreeNodeVar[8] == 3) {
                Object o = null;
            }

            // check if we found previous external node with the same generation
            Object[] previousExternalFreeNodeVar = tailCursor.getPreviousExternalFreeNode();
            if (localFreeNodeVar == previousExternalFreeNodeVar) {
                return getExternalFreeNode(tailCursor);
                /*int gen = Node.currentGeneration(previousExternalFreeNodeVar);
                if (gen == tailCursor.getPreviousExternalFreeNodeGen()) {
                    //localFreeNodeVar = null;
                    return getExternalFreeNode(tailCursor);
                } else {
                    Object o = null;
                }*/
            }

            //Node.setNextFree(localFreeNodeVar, null);

            MemoryFence.load();

            Object prev = Node.getElement(localFreeNodeVar, 0);
            if (prev != null) {
                Object o = null;
            }

            Object[] nextNode = Node.getNextFree(localFreeNodeVar);

            Object prev2 = Node.getElement(nextNode, 0);
            if (prev != null) {
                Object o = null;
            }

            tailCursor.setLocalFreeNode(nextNode);
            return localFreeNodeVar;
        }

        // ensure we do not load anything before we check local free nodes
        MemoryFence.load();

        return getExternalFreeNode(tailCursor);
    }

    private static Object[] getExternalFreeNode(TailCursor tailCursor) {
        // externalFreeNode will be accessed from another thread so we load it only once
        Object[] externalFreeNodeVar = tailCursor.getExternalFreeNode();

        MemoryFence.load();

        // if external free node is still not changed we assume that there are not enough nodes and we have to create new
        //int currentExternalFreeNodeGenVar = tailCursor.getCurrentExternalFreeNodeGen();
        Object[] currentExternalFreeNodeVar = tailCursor.getCurrentExternalFreeNode();
        //int gen = Node.currentGeneration(externalFreeNodeVar);
        if (externalFreeNodeVar == currentExternalFreeNodeVar/* && gen == currentExternalFreeNodeGenVar*/) {
            //tailCursor.setLocalFreeNode(null);
            return Node.createNew();
        }

        if ((Integer) externalFreeNodeVar[8] == 3) {
            Object o = null;
        }

        // save external nodes
        //tailCursor.setPreviousExternalFreeNodeGen(currentExternalFreeNodeGenVar);
        tailCursor.setPreviousExternalFreeNode(currentExternalFreeNodeVar);
        //tailCursor.setCurrentExternalFreeNodeGen(gen);
        tailCursor.setCurrentExternalFreeNode(externalFreeNodeVar);

        MemoryFence.load();

        Object[] nextNode = Node.getNextFree(externalFreeNodeVar);
        //Node.setNextFree(externalFreeNodeVar, null);
        tailCursor.setLocalFreeNode(nextNode);

        Object prev = Node.getElement(externalFreeNodeVar, 0);
        if (prev != null) {
            Object o = null;
        }

        Object prev2 = Node.getElement(nextNode, 0);
        if (prev != null) {
            Object o = null;
        }

        return externalFreeNodeVar;
    }
}
