package io.github.thepun.pq;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;

final class Persister implements Runnable {

    private final FileChannel file;
    private final WriteBuffer buffer;
    private final QueueToPersister.Head[] inputs;

    private boolean stopped;

    Persister(Configuration configuration, PersistentQueueTail<?, ?>[] tails) {
        inputs = null;

        stopped = false;
        buffer = new WriteBuffer();

        try {
            file = FileChannel.open(FileSystems.getDefault().getPath(configuration.getDataPath()), StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new PersistenceException("Failed to open data file: " + configuration.getDataPath(), e);
        }
    }

    @Override
    public void run() {
        try {
            process();
        } catch (Throwable e) {
            close();
            throw e;
        }
    }

    void deactivate() {
        stopped = false;
    }

    private void process() {
        int batchSize = 0;
        Object[] batch = new Object[16];

        int inputIndex = 0;
        QueueToPersister.Head[] inputsVar = inputs;

        int mappedMemoryIndex = 0;
        MappedByteBuffer[] mappedMemory = new MappedByteBuffer[16];
        fulfillMappedMemmory(mappedMemory);

        for (; ; ) {
            if (stopped) {
                return;
            }

            QueueToPersister.Head input = inputsVar[inputIndex++];


        }
    }

    private void fulfillMappedMemmory(MappedByteBuffer[] mappedMemory) {

    }

    private void close() {
        try {
            file.close();
        } catch (IOException e) {
            // just ignore
        }
    }

}
