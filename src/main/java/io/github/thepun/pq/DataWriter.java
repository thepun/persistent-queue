package io.github.thepun.pq;

import io.github.thepun.unsafe.chars.OffHeapCharSequence;

import java.nio.MappedByteBuffer;

final class DataWriter implements WriteBuffer {

    private final int size;
    private final MappedByteBuffer buffer;

    private int cursor;
    private int sizeMinusEight;

    DataWriter(MappedByteBuffer buffer) {
        this.buffer = buffer;

        size = buffer.capacity();
        sizeMinusEight = size - 8;
    }

    @Override
    public void writeInt(int value) {
        int index = cursor % size;
        if (index < sizeMinusEight) {
            buffer.putInt(index, value);
        } else {

        }
    }

    @Override
    public void writeLong(long value) {

    }

    @Override
    public void writeString(String value) {

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

    void setCursor(long cursor) {

    }

    long getCursor() {
        return cursor;
    }
}
