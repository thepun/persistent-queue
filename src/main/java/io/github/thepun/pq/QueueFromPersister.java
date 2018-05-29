package io.github.thepun.pq;

import sun.misc.Contended;

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

        void add(Object[] buffer, int offset, int length) {

        }

    }
}
