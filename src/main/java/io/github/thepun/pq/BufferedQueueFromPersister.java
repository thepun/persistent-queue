package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;
import io.github.thepun.unsafe.TypeSize;
import sun.misc.Contended;

@Contended
final class BufferedQueueFromPersister implements PersistentQueueHead<Object> {

    private static final int BUFFER_USER_SIZE = 1024;
    private static final int BUFFER_USER_SIZE_MASK = BUFFER_USER_SIZE - 1;
    private static final int BUFFER_OFFSET = 64 / TypeSize.REFERENCE_;
    private static final int BUFFER_SIZE = BUFFER_USER_SIZE + 2 * BUFFER_OFFSET;
    private static final int BUFFER_USER_START_INDEX = BUFFER_OFFSET;


    private final Tail tail;
    private final Object[] buffer;

    private Data data;
    private Commit commit;
    private Sequence sequence;
    private long sequenceId;

    @Contended
    private long readIndex;

    BufferedQueueFromPersister(Configuration<Object, Object> configuration) {
        tail = new Tail(this);
        buffer = new Object[BUFFER_SIZE];
    }

    @Override
    public int get(Object[] batch, int offset, int length) {
        Object[] bufferVar = buffer;
        long readIndexVar = readIndex;
        long writeIndexVar = tail.writeIndex;

        // no elements in the buffer
        if (writeIndexVar == readIndexVar) {
            return 0;
        }

        // check if there is enough space in batch
        int difference = (int) (writeIndexVar - readIndexVar);
        if (difference > length) {
            difference = length;
        }

        int readBufferIndex = (int) (readIndexVar & BUFFER_USER_SIZE_MASK);
        int writeBufferIndex = (int) (writeIndexVar & BUFFER_USER_SIZE_MASK);

        // check fast path
        if (readBufferIndex < writeBufferIndex) {
            // fast path: we can copy memory by single region
            System.arraycopy(bufferVar, readBufferIndex + BUFFER_USER_START_INDEX, batch, offset, difference);
        } else {
            // slow path: copy by two regions
            // first region size
            int partSize = BUFFER_USER_SIZE - readBufferIndex;

            // check if there is enough space in batch
            if (partSize > difference) {
                // copy just first region
                System.arraycopy(bufferVar, readBufferIndex + BUFFER_USER_START_INDEX, batch, offset, difference);
            } else {
                int rest = difference - partSize;

                // copy first region
                System.arraycopy(bufferVar, readBufferIndex + BUFFER_USER_START_INDEX, batch, offset, partSize);

                // copy second region
                System.arraycopy(bufferVar, writeBufferIndex + BUFFER_USER_START_INDEX, batch, offset, rest);
            }
        }

        // change read cursor
        MemoryFence.store(); // change read index only after all data is copied
        readIndex += difference;
        return difference;
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
        // sync persister buffer
        data.sync();
        sequence.sync();

        // sync commit buffer
        commit.mark(sequenceId);
        commit.sync();
    }

    Tail getTail() {
        return tail;
    }

    @Contended
    static final class Tail {

        private final BufferedQueueFromPersister head;
        private final Object[] buffer;

        @Contended
        private long writeIndex;

        Tail(BufferedQueueFromPersister head) {
            this.head = head;
            buffer = head.buffer;
        }

        void setSequence(Sequence sequence) {
            head.sequence = sequence;
        }

        void setCommit(Commit commit) {
            head.commit = commit;
        }

        void setData(Data data) {
            head.data = data;
        }

        void setSequenceId(long sequenceId) {
            head.sequenceId = sequenceId;
        }

        // batch contains context objects so copy only each even element
        int add(Object[] pairBatch, int batchOffset, int pairCount) {
            Object[] bufferVar = buffer;
            long writeIndexVar = writeIndex;
            long readIndexVar = head.readIndex;

            // no elements in the buffer
            if (writeIndexVar == readIndexVar + BUFFER_USER_SIZE) {
                return 0;
            }

            // check if there is enough space in buffer
            int difference = (int) (writeIndexVar - readIndexVar);
            if (difference > pairCount) {
                difference = pairCount;
            }

            // copy elements one by one skipping odd elements
            int bufferIndex = ((int)(writeIndexVar & BUFFER_USER_SIZE_MASK)) + BUFFER_USER_START_INDEX;
            int batchBoundary = difference * 2 + batchOffset;
            for (int batchIndex = batchOffset; batchIndex < batchBoundary; batchIndex += 2) {
                Object element = pairBatch[batchIndex];
                bufferVar[bufferIndex++] = element;
            }

            // change write cursor
            MemoryFence.store(); // change write index only after all data is copied
            writeIndex += difference;
            return difference;
        }
    }
}
