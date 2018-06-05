package io.github.thepun.pq;

import io.github.thepun.unsafe.OffHeapMemory;
import io.github.thepun.unsafe.chars.OffHeapCharSequence;

import java.nio.MappedByteBuffer;

final class DataReader implements ReadBuffer {

    private final MappedByteBuffer buffer;

    private long cursor;
    private long limit;

    DataReader(Data data) {
        buffer = data.getBuffer();
    }

    @Override
    public byte readByte() {
        if (cursor == limit) {
            throw new IndexOutOfBoundsException();
        }

        byte value = buffer.get((int)cursor);
        cursor++;
        return value;
    }

    @Override
    public char readChar() {
        if (cursor + 2 >= limit) {
            throw new IndexOutOfBoundsException();
        }

        char value = buffer.getChar((int)cursor);
        cursor += 2;
        return value;
    }

    @Override
    public short readShort() {
        if (cursor + 2 >= limit) {
            throw new IndexOutOfBoundsException();
        }

        short value = buffer.getShort((int)cursor);
        cursor += 2;
        return value;
    }

    @Override
    public int readInt() {
        if (cursor + 4 >= limit) {
            throw new IndexOutOfBoundsException();
        }

        int value = buffer.getInt((int)cursor);
        cursor += 4;
        return value;
    }

    @Override
    public long readLong() {
        if (cursor + 8 >= limit) {
            throw new IndexOutOfBoundsException();
        }

        long value = buffer.getInt((int)cursor);
        cursor += 8;
        return value;
    }

    @Override
    public boolean readBoolean() {
        if (cursor == limit) {
            throw new IndexOutOfBoundsException();
        }

        byte value = buffer.get((int)cursor);
        cursor++;
        return value == 1;
    }

    @Override
    public void readOffHeap(long address, int length) {
        if (cursor + length >= limit) {
            throw new IndexOutOfBoundsException();
        }

        for (int i = 0; i < length; i++) {
            OffHeapMemory.setByte(address + i, buffer.get((int)(cursor + i)));
        }

        cursor += length;
    }

    @Override
    public void readOffHeap(OffHeapCharSequence offHeapCharSequence) {
        readOffHeap(offHeapCharSequence.getOffheapAddress(), offHeapCharSequence.getOffheapLength());
    }

    void setCursor(long cursor) {
        this.cursor = cursor;
    }

    void setLimit(long limit) {
        this.limit = limit;
    }
}
