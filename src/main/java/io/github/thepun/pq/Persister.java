package io.github.thepun.pq;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;

final class Persister implements Runnable {

    private final WriteBuffer buffer;
    private final FileChannel file;
    private final MappedByteBuffer[] mappedMemory;
    private final PersistentQueueTail<?, ?>[] tails;

    private boolean active;

    Persister(Configuration configuration, PersistentQueueTail<?, ?>[] tails) {
        this.tails = tails;

        active = true;
        buffer = new WriteBuffer();
        mappedMemory = new MappedByteBuffer[16];

        try {
            file = FileChannel.open(FileSystems.getDefault().getPath(configuration.getDataPath()), StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new PersistenceException("Failed to open data file: " + configuration.getDataPath(), e);
        }
    }

    @Override
    public void run() {
        for (;;) {

        }
    }

    void deactivate() {
        active = false;
    }

    private void fulfillMappedMemmory() {

    }
}
