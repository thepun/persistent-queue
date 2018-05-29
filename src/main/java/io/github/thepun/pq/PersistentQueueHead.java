package io.github.thepun.pq;

public interface PersistentQueueHead<T> {

    int get(T[] batch);

    int getOrWait(T[] batch);

}
