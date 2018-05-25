package io.github.thepun.pq;

public interface PersistentQueueTail<T, C> {

    void add(T element, C elementContext);

}
