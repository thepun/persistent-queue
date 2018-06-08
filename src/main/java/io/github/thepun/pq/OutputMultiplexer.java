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
            long lastSequenceId = input.getLastSequenceId();

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

                MemoryFence.load();

                // check we need to move to another node
                long elementNodeIndex = readIndexVar >> Node.NODE_DATA_SHIFT;
                if (elementNodeIndex != nodeIndexVar) {
                    // try get next node from chain
                    Object[] nextNode = Node.getNext(currentNodeVar);
                    if (nextNode == null) {
                        break;
                    }

                    MemoryFence.load();

                    // clear previous node and chan it with current
                    Object[] freeNodeVar = input.getFreeNode();
                    Node.clear(freeNodeVar);
                    Node.setNextFree(currentNodeVar, null);

                    // ensure we expose free node only after it is prepared
                    MemoryFence.store();

                    // mark previous node as reusable
                    Node.setNextFree(freeNodeVar, currentNodeVar);

                    // use new node as current
                    currentNodeVar = nextNode;
                    nodeIndexVar = elementNodeIndex;
                    input.setFreeNode(currentNodeVar);
                    input.setNodeIndex(elementNodeIndex);
                    input.setCurrentNode(currentNodeVar);
                }

                int elementIndex = (int) (readIndexVar & Node.NODE_DATA_SIZE_MASK);
                Object element = Node.getElement(currentNodeVar, elementIndex);
                if (element == null) {
                    // another thread didn't write to the index yet
                    break;
                }

                MemoryFence.load();

                // just take element without context
                batch[batchIndex] = element;

                // counters for next step
                lastSequenceId += 1;
                readIndexVar += 2;
                batchIndex += 1;

                // check we have space in batch
                if (batchIndex == batchBoundery) {
                    input.setLastSequenceId(lastSequenceId);
                    input.setCursor(readIndexVar);
                    pipelineIndex = pipelineIndexVar;
                    return length;
                }
            }

            input.setLastSequenceId(lastSequenceId);
            input.setCursor(readIndexVar);
            pipelineIndexVar++;
        } while (pipelineIndexVar != maxPipelineIndex);

        pipelineIndex = pipelineIndexVar;
        return batchIndex - offset;
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
        HeadCursor[] inputsVar = inputs;
        int pipelineSize = inputsVar.length;

        for (int i = 0; i < pipelineSize; i++) {
            HeadCursor input = inputsVar[i];

            // sync data
            input.getData().sync();

            // sync sequence
            Sequence sequence = input.getSequence();
            sequence.markLastCommitted(input.getLastSequenceId());
            sequence.sync();
        }
    }
}
