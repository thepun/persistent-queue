package io.github.thepun.pq;

public interface PersistentQueueHead<T> {

    int get(T[] batch, int offset, int length);

    int getOrWait(T[] batch, int offset, int length);

    void commit();

}
