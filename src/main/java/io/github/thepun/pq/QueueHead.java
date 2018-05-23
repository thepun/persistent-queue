package io.github.thepun.pq;

public interface QueueHead {

    Object get();
    Object getOrWait();
    void commit();

}
