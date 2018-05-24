package io.github.thepun.pq;

public final class PersistentQueue {

    private final PersistentQueueHead head;
    private final PersistentQueueTail[] tails;

    private Thread persisterThread;

    public PersistentQueue(Configuration configuration) {
        head = new PersistentQueueHead(configuration);

        PersistentQueueTail[] tailsToUse = new PersistentQueueTail[configuration.getTailCount()];
        for (int i = 0; i < tailsToUse.length; i++) {
            tailsToUse[i] = new PersistentQueueTail(configuration);
        }
        tails = tailsToUse;
    }

    public PersistentQueueHead getHead() {
        return head;
    }

    public PersistentQueueTail getTail(int index) {
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
