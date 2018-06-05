package io.github.thepun.pq;

import io.github.thepun.unsafe.chars.OffHeapCharSequence;

public interface WriteBuffer {

    void writeByte(byte value);
    void writeChar(char value);
    void writeShort(short value);
    void writeInt(int value);
    void writeLong(long value);
    void writeBoolean(boolean value);
    void writeOffHeap(long address, int length);
    void writeOffHeap(OffHeapCharSequence offHeapCharSequence);

}
