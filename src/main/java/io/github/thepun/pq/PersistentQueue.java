package io.github.thepun.pq;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class PersistentQueue<T, C> {

    private final PersistentQueueHead<T>[] heads;
    private final PersistentQueueTail<T, C>[] tails;

    private Thread persisterThread;

    public PersistentQueue(Configuration<T, C> configuration) {
        PersistentQueueHead<T>[] headsToUse = new PersistentQueueHead[configuration.getHeadCount()];
        for (int i = 0; i < headsToUse.length; i++) {
            headsToUse[i] = (PersistentQueueHead<T>) new QueueFromPersister((Configuration<Object, Object>) configuration);
        }
        heads = headsToUse;

        PersistentQueueTail<T, C>[] tailsToUse = new PersistentQueueTail[configuration.getTailCount()];
        for (int i = 0; i < tailsToUse.length; i++) {
            tailsToUse[i] = (PersistentQueueTail<T, C>) new QueueToPersister((Configuration<Object, Object>) configuration);
        }
        tails = tailsToUse;
    }

    public PersistentQueueHead<T> getHead(int index) {
        if (index < 0 || index > heads.length) {
            throw new IllegalArgumentException("Wrong head index");
        }

        return heads[index];
    }

    public PersistentQueueTail<T, C> getTail(int index) {
        if (index < 0 || index > tails.length) {
            throw new IllegalArgumentException("Wrong tail index");
        }

        return tails[index];
    }

    public synchronized void start() {

    }

    public synchronized void stop() {

    }


}
