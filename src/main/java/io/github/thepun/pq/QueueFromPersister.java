package io.github.thepun.pq;

import sun.misc.Contended;

final class QueueFromPersister implements PersistentQueueHead<Object> {


    private final Tail tail;

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

    Tail getTail() {
        return tail;
    }


    @Contended
    static final class Tail {

        private long sequenceId;
        private Commit commit;

        void setCommit(Commit commit) {
            this.commit = commit;
        }

        void setSequenceId(long sequenceId) {
            this.sequenceId = sequenceId;
        }

        void add(Object[] buffer, int offset, int length) {
            commit.mark(sequenceId);


        }
    }
}
