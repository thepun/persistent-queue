package io.github.thepun.pq;

import io.github.thepun.unsafe.chars.OffHeapCharSequence;

import java.nio.channels.FileChannel;

final class PersisterWriteBuffer implements WriteBuffer {

    PersisterWriteBuffer(String dataPath) {

        /*

        try {
            fileChannel = FileChannel.open(FileSystems.getDefault().getPath(configuration.getDataPath()), StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new PersistenceException("Failed to open data file: " + configuration.getDataPath(), e);
        }

        */
    }

    @Override
    public void writeInt(int value) {

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

    void sync() {

    }

    void close() {

    }
}
