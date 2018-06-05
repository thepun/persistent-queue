package io.github.thepun.pq;

import io.github.thepun.unsafe.chars.OffHeapCharSequence;

final class DataReader implements ReadBuffer {

    DataReader(Data data) {

    }

    void setCursor(long cursor) {

    }

    void setLimit(long limit) {

    }

    @Override
    public byte readByte() {
        return 0;
    }

    @Override
    public char readChar() {
        return 0;
    }

    @Override
    public short readShort() {
        return 0;
    }

    @Override
    public int readInt() {
        return 0;
    }

    @Override
    public long readLong() {
        return 0;
    }

    @Override
    public boolean readBoolean() {
        return false;
    }

    @Override
    public void readOffHeap(long address, int length) {

    }

    @Override
    public void readOffHeap(OffHeapCharSequence offHeapCharSequence) {

    }
}
