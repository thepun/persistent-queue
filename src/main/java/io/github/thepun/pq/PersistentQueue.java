package io.github.thepun.pq;

import java.util.stream.Stream;

public final class PersistentQueue<T, C> {

    private final Persister persister;
    private final Pipeline[] tails;
    private final BufferedQueueFromPersister[] heads;

    public PersistentQueue(Configuration<T, C> configuration) throws PersistenceException {
        Configuration<Object, Object> objectConfiguration = (Configuration<Object, Object>) configuration;

        BufferedQueueFromPersister[] headsToUse = new BufferedQueueFromPersister[configuration.getHeadCount()];
        for (int i = 0; i < headsToUse.length; i++) {
            headsToUse[i] = new BufferedQueueFromPersister(objectConfiguration);
        }
        heads = headsToUse;

        Pipeline[] tailsToUse = new Pipeline[configuration.getTailCount()];
        for (int i = 0; i < tailsToUse.length; i++) {
            tailsToUse[i] = new Pipeline(objectConfiguration);
        }
        tails = tailsToUse;

        // scan files
        Scanner scanner = new Scanner(objectConfiguration);
        ScanResult scanResult = scanner.scan();

        // persister
        Pipeline.Head[] inputs = Stream.of(heads).map(BufferedQueueFromPersister::getTail).toArray(Pipeline.Head[]::new);
        BufferedQueueFromPersister.Tail[] outputs = Stream.of(tails).map(Pipeline::getHead).toArray(BufferedQueueFromPersister.Tail[]::new);
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
