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

        long elementNodeIndex = writerIndexVar >> Node.NODE_DATA_SHIFT;
        if (elementNodeIndex != nodeIndexVar) {
            // get new node
            Object[] newNode = getFreeNode(tailCursorVar);

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
        Object[] externalFreeNodeVar = tailCursor.getFreeNode();

        Object[] nextNode = Node.getNextFree(externalFreeNodeVar);
        if (nextNode == null) {
            return Node.createNew();
        }

        tailCursor.setFreeNode(nextNode);
        return externalFreeNodeVar;
    }
}
