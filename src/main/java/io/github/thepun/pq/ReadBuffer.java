package io.github.thepun.pq;

import io.github.thepun.unsafe.chars.OffHeapCharSequence;

public interface ReadBuffer {

    byte readByte();
    char readChar();
    short readShort();
    int readInt();
    long readLong();
    boolean readBoolean();
    void readOffHeap(long address, int length);
    void readOffHeap(OffHeapCharSequence offHeapCharSequence);

}
