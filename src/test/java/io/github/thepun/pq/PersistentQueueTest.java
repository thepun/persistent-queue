package io.github.thepun.pq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistentQueueTest {

    private Configuration<Object, Object> configuration;

    @BeforeEach
    void prepareCOnfiguration() throws IOException {
        configuration = new Configuration<>();
        configuration.setPersistCallback((t, c) -> {});
        configuration.setDataPath(Files.createTempDirectory("pq").toAbsolutePath().toString());
        configuration.setPersisterThreadFactory(Thread::new);
        configuration.setSerializers(new HashMap<>());
        configuration.setSequenceFileSize(8 + 100 * 32);
        configuration.setDataFileSize(1000);
        configuration.setHeadCount(1);
        configuration.setTailCount(1);
    }

    @Test
    void createStartAndStop() throws PersistenceException {
        PersistentQueue<Object, Object> persistentQueue = new PersistentQueue<>(configuration);
        persistentQueue.start();
        persistentQueue.stop();
    }

    @Test
    void pushAndPull() throws PersistenceException {
        configuration.getSerializers().put(Integer.class, new IntMarshaler());

        PersistentQueue<Object, Object> persistentQueue = new PersistentQueue<>(configuration);
        persistentQueue.start();

        // push
        persistentQueue.getTail(0).add(123, null);

        // pull
        Object[] batch = new Object[1];
        int result = persistentQueue.getHead(0).getOrWait(batch, 0, 1);
        assertEquals(1, result);
        assertEquals(123, batch[0]);

        persistentQueue.stop();
    }

    @Test
    void pushAndPullMillion() throws PersistenceException {
        configuration.getSerializers().put(Integer.class, new IntMarshaler());

        PersistentQueue<Object, Object> persistentQueue = new PersistentQueue<>(configuration);
        persistentQueue.start();

        // push
        PersistentQueueTail<Object, Object> tail = persistentQueue.getTail(0);
        for (int i = 0; i < 10000000; i++) {
            tail.add(i, null);
        }

        // pull
        Object[] batch = new Object[MathUtil.nextGreaterPrime(100)];
        PersistentQueueHead<Object> head = persistentQueue.getHead(0);
        int i = 0;
        do {
            int result = head.getOrWait(batch, 0, batch.length);
            for (int k = 0; k < result; k++, i++) {
                assertEquals(i, batch[k]);
            }
        } while (i < 10000000);

        persistentQueue.stop();
    }

    @Test
    void pushAndPullAfterRestart() throws PersistenceException, InterruptedException {
        configuration.getSerializers().put(Integer.class, new IntMarshaler());

        PersistentQueue<Object, Object> persistentQueue = new PersistentQueue<>(configuration);
        persistentQueue.start();

        // push
        persistentQueue.getTail(0).add(123, null);
        Thread.sleep(1000);

        // restart
        persistentQueue.stop();
        persistentQueue = new PersistentQueue<>(configuration);
        persistentQueue.start();

        // pull
        Object[] batch = new Object[1];
        int result = persistentQueue.getHead(0).getOrWait(batch, 0, 1);
        assertEquals(1, result);
        assertEquals(123, batch[0]);
    }


    private class IntMarshaler implements Marshaller<Object, Object> {

        @Override
        public int getTypeId() {
            return 1;
        }

        @Override
        public void serialize(WriteBuffer buffer, Object object, Object objectContext) {
            buffer.writeInt((Integer) object);
        }

        @Override
        public Object deserialize(ReadBuffer buffer) {
            return buffer.readInt();
        }
    }
}
