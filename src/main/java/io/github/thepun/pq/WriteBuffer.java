package io.github.thepun.pq;

import io.github.thepun.unsafe.chars.OffHeapCharSequence;

public interface WriteBuffer {

    void writeInt(int value);
    void writeLong(long value);
    void writeString(String value);
    void writeBoolean(boolean value);
    void writeOffHeap(long address, int length);
    void writeOffHeap(OffHeapCharSequence offHeapCharSequence);

}
