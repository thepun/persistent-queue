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

                // check we need to move to another node
                long elementNodeIndex = readIndexVar >> NodeUtil.NODE_DATA_SHIFT;
                if (elementNodeIndex != nodeIndexVar) {
                    // try get next node from chain
                    Object[] nextNode = NodeUtil.getNextNode(currentNodeVar);
                    if (nextNode == null) {
                        break;
                    }

                    // free processed node
                    NodeUtil.setNextFreeNode(currentNodeVar, input.getFreeNode());
                    NodeUtil.incrementGeneration(currentNodeVar);

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
                    break;
                }

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
                    return length;
                }
            }

            input.setLastSequenceId(lastSequenceId);
            input.setCursor(readIndexVar);
            pipelineIndex++;
        } while (pipelineIndex != maxPipelineIndex);

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
