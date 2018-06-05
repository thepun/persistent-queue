package io.github.thepun.pq;

import io.github.thepun.unsafe.chars.OffHeapCharSequence;

import java.nio.MappedByteBuffer;

final class DataWriter implements WriteBuffer {

    private final int size;
    private final MappedByteBuffer buffer;

    private long cursor;

    DataWriter(Data data) {
        buffer = data.getBuffer();
        size = buffer.capacity();
    }

    @Override
    public void writeByte(byte value) {

    }

    @Override
    public void writeChar(char value) {

    }

    @Override
    public void writeShort(short value) {

    }

    @Override
    public void writeInt(int value) {

    }

    @Override
    public void writeLong(long value) {

    }

    @Override
    public void writeBoolean(boolean value) {

    }

    @Override
    public void writeOffHeap(long address, int length) {

    }

    @Override
    public void writeOffHeap(OffHeapCharSequence offHeapCharSequence) {

    }

    void mark(long sequenceId) {

    }

    void commit(long sequenceId) {

    }

    void setCursor(long cursor) {
        this.cursor = cursor;
    }

    long getCursor() {
        return cursor;
    }
}
