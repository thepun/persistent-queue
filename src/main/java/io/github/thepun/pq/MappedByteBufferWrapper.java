package io.github.thepun.pq;

import io.github.thepun.unsafe.chars.OffHeapCharSequence;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;

final class MappedByteBufferWrapper implements WriteBuffer {

    private final int size;
    private final MappedByteBuffer buffer;

    private int cursor;
    private int sizeMinusEight;

    MappedByteBufferWrapper(MappedByteBuffer buffer) {
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

    void setCursor(long cursor) {

    }

    long getCursor() {
        return cursor;
    }
}
