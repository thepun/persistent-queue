package io.github.thepun.pq;

public interface QueueHead {

    Object take();
    void commitLast();

}
