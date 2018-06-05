package io.github.thepun.pq;

import org.junit.jupiter.api.Test;

class PersistentQueueTest {

    @Test
    void createStartAndStop() throws PersistenceException {
        Configuration<Object, Object> configuration = new Configuration<>();

        PersistentQueue<Object, Object> persistentQueue = new PersistentQueue<>(configuration);
        persistentQueue.start();
        persistentQueue.stop();
    }

}
