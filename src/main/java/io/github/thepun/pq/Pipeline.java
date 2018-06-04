package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;
import sun.misc.Contended;

@Contended
final class Pipeline {

    private final TailCursor tailCursor;
    private final HeadCursor headCursor;
    private final SerializerCursor serializerCursor;

    Pipeline() {
        Object[] currentNode = NodeUtil.createNewNode();
        Object[] localFreeNode = NodeUtil.createNewNode();
        Object[] externalFreeNode = NodeUtil.createNewNode();
        Object[] previousExternalFreeNode = NodeUtil.createNewNode();
        externalFreeNode[NodeUtil.NEXT_FREE_NODE_INDEX] = previousExternalFreeNode;

        tailCursor = new TailCursor();
        tailCursor.setCurrentNode(currentNode);
        tailCursor.setLocalFreeNode(localFreeNode);
        tailCursor.setExternalFreeNode(externalFreeNode);
        tailCursor.setCurrentExternalFreeNode(externalFreeNode);
        tailCursor.setPreviousExternalFreeNode(previousExternalFreeNode);

        serializerCursor = new SerializerCursor(tailCursor);
        serializerCursor.setCurrentNode(currentNode);

        headCursor = new HeadCursor(tailCursor, serializerCursor);
        headCursor.setCurrentNode(currentNode);
        headCursor.setFreeNode(externalFreeNode);

        MemoryFence.full();
    }

    TailCursor getTailCursor() {
        return tailCursor;
    }

    HeadCursor getHeadCursor() {
        return headCursor;
    }

    SerializerCursor getSerializerCursor() {
        return serializerCursor;
    }
}
