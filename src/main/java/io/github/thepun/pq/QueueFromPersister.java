package io.github.thepun.pq;

import sun.misc.Contended;

import java.nio.MappedByteBuffer;

final class QueueFromPersister implements PersistentQueueHead<Object> {



    QueueFromPersister(Configuration<Object, Object> configuration) {

    }

    @Override
    public int get(Object[] batch) {
        return 0;
    }

    @Override
    public int getOrWait(Object[] batch) {
        return 0;
    }


    @Contended
    static final class Tail {

        private long sequenceId;
        private Commit commit;

        void setCommit(Commit commit) {

        }

        void setSequenceId(long sequenceId) {

        }

        void add(Object[] buffer, int offset, int length) {

        }
    }
}
