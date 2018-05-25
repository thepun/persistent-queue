package io.github.thepun.pq;

final class Persister implements Runnable {

    private final PersistentQueueTail<?, ?>[] tails;

    private boolean active;

    Persister(PersistentQueueTail<?, ?>[] tails) {
        this.tails = tails;

        active = true;
    }

    @Override
    public void run() {
        while (active) {


        }
    }
}
