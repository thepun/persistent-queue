package io.github.thepun.pq;

import java.util.stream.Stream;

public final class PersistentQueue<T, C> {

    private final Persister persister;
    private final QueueToPersister[] tails;
    private final QueueFromPersister[] heads;

    public PersistentQueue(Configuration<T, C> configuration) throws PersistenceException {
        Configuration<Object, Object> objectConfiguration = (Configuration<Object, Object>) configuration;

        QueueFromPersister[] headsToUse = new QueueFromPersister[configuration.getHeadCount()];
        for (int i = 0; i < headsToUse.length; i++) {
            headsToUse[i] = new QueueFromPersister(objectConfiguration);
        }
        heads = headsToUse;

        QueueToPersister[] tailsToUse = new QueueToPersister[configuration.getTailCount()];
        for (int i = 0; i < tailsToUse.length; i++) {
            tailsToUse[i] = new QueueToPersister(objectConfiguration);
        }
        tails = tailsToUse;

        // scan files
        Scanner scanner = new Scanner(objectConfiguration);
        ScanResult scanResult = scanner.scan();

        // persister
        QueueToPersister.Head[] inputs = Stream.of(heads).map(QueueFromPersister::getTail).toArray(QueueToPersister.Head[]::new);
        QueueFromPersister.Tail[] outputs = Stream.of(tails).map(QueueToPersister::getHead).toArray(QueueFromPersister.Tail[]::new);
        persister = new Persister(inputs, outputs, scanResult, objectConfiguration);
    }

    public PersistentQueueHead<T> getHead(int index) {
        if (index < 0 || index > heads.length) {
            throw new IllegalArgumentException("Wrong head index");
        }

        return (PersistentQueueHead<T>) heads[index];
    }

    public PersistentQueueTail<T, C> getTail(int index) {
        if (index < 0 || index > tails.length) {
            throw new IllegalArgumentException("Wrong tail index");
        }

        return (PersistentQueueTail<T, C>) tails[index];
    }

    public void deactivate() {
        persister.deactivate();
    }
}
