package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;

final class OutputMultiplexer implements PersistentQueueHead<Object> {

    private final HeadCursor[] inputs;

    private long pipelineIndex;

    OutputMultiplexer(HeadCursor[] inputs) {
        this.inputs = inputs;
    }

    @Override
    public int get(Object[] batch, int offset, int length) {
        HeadCursor[] inputsVar = inputs;
        long pipelineIndexVar = pipelineIndex;
        int pipelineSize = inputsVar.length;
        long maxPipelineIndex = pipelineIndexVar + pipelineSize;

        int batchIndex = offset;
        int batchBoundery = offset + length;

        do {
            HeadCursor input = inputsVar[(int) (pipelineIndexVar % pipelineSize)];

            Object[] currentNodeVar = input.getCurrentNode();
            long nodeIndexVar = input.getNodeIndex();
            long readIndexVar = input.getCursor();

            SerializerCursor limit = input.getSerializerCursor();
            long limitCursor = limit.getCursor();

            for (; ; ) {
                // check we have elements before limit
                if (readIndexVar == limitCursor) {
                    break;
                }

                // check we need to move to another node
                long elementNodeIndex = readIndexVar >> NodeUtil.NODE_DATA_SHIFT;
                if (elementNodeIndex != nodeIndexVar) {
                    // try get next node from chain
                    Object[] nextNode = (Object[]) currentNodeVar[NodeUtil.NEXT_NODE_INDEX];
                    if (nextNode == null) {
                        input.setCursor(readIndexVar);
                        break;
                    }

                    // free processed node
                    currentNodeVar[NodeUtil.NEXT_FREE_NODE_INDEX] = input.getFreeNode();
                    ((Generation) currentNodeVar[NodeUtil.NODE_GENERATION_INDEX]).increment();

                    // ensure we expose free node only after it is prepared
                    MemoryFence.store();

                    // expose new free node
                    input.getTailCursor().setExternalFreeNode(currentNodeVar);

                    // use new node as current
                    currentNodeVar = nextNode;
                    input.setNodeIndex(elementNodeIndex);
                    input.setCurrentNode(currentNodeVar);
                }

                int elementIndex = (int) (readIndexVar & NodeUtil.NODE_DATA_SIZE_MASK);
                Object element = currentNodeVar[elementIndex];
                if (element == null) {
                    // another thread didn't write to the index yet
                    input.setCursor(readIndexVar);
                    break;
                }

                // just take element without context
                batch[batchIndex] = element;

                // counters for next step
                readIndexVar += 2;
                batchIndex += 1;
                input.setCursor(readIndexVar);

                // check we have space in batch
                if (batchIndex == batchBoundery) {
                    return length;
                }
            }

            pipelineIndex++;
        } while (pipelineIndex != maxPipelineIndex);

        return batchBoundery - batchIndex;
    }

    // TODO: add park/unpark on wait
    @Override
    public int getOrWait(Object[] batch, int offset, int length) {
        int result;
        do {
            result = get(batch, offset, length);
        } while (result == 0);

        return result;
    }

    @Override
    public void commit() {

    }

    /*int get(Object[] buffer, int offset, int length) {
        Object[] currentNodeVar = currentNode;
        long readIndexVar = cursor;
        long nodeIndexVar = nodeIndex;

        int count = 0;
        int bufferIndex = offset;
        do {
            // check we need to move to another node
            long elementNodeIndex = readIndexVar >> NODE_DATA_SHIFT;
            if (elementNodeIndex != nodeIndexVar) {
                // try get next node from chain
                Object[] nextNode = (Object[]) currentNodeVar[NEXT_NODE_INDEX];
                if (nextNode == null) {
                    cursor = readIndexVar;
                    return count;
                }

                // free processed node
                currentNodeVar[NEXT_FREE_NODE_INDEX] = freeNode;
                ((Generation) currentNodeVar[NODE_GENERATION_INDEX]).increment();

                // ensure we expose free node only after it is prepared
                MemoryFence.store();

                // expose new free node
                pipeline.externalFreeNode = currentNodeVar;

                // use new node as current
                currentNodeVar = nextNode;
                nodeIndex = elementNodeIndex;
                currentNode = currentNodeVar;
            }

            int elementIndex = (int) (readIndexVar & NODE_DATA_SIZE_MASK);
            Object element = currentNodeVar[elementIndex];
            if (element == null) {
                // another thread didn't write to the index yet
                cursor = readIndexVar;
                return count;
            }

            buffer[bufferIndex] = element;
            buffer[bufferIndex | 1] = currentNodeVar[elementIndex | 1];

            // counters for next step
            readIndexVar += 2;
            bufferIndex += 2;
            count += 1;
        } while (count < length);

        cursor = readIndexVar;
        return count;
    }*/
}
