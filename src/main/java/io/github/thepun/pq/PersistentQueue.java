package io.github.thepun.pq;

import java.util.stream.Stream;

public final class PersistentQueue<T, C> {

    private final Thread thread;
    private final Persister persister;
    private final QueueToPersister[] tails;
    private final OutputMultiplexer[] heads;

    public PersistentQueue(Configuration<T, C> configuration) throws PersistenceException {
        Configuration<Object, Object> objectConfiguration = (Configuration<Object, Object>) configuration;

        if ((configuration.getSequenceFileSize() - 8) % 32 != 0) {
            throw new PersistenceException("Incorrect sequence file size: " + configuration.getSequenceFileSize() + " is not equal to X * 32 + 8");
        }

        if (configuration.getHeadCount() > configuration.getTailCount()) {
            throw new PersistenceException("Incorrect head count: should be less or equals to tail count " + configuration.getTailCount());
        }

        int inputsPerOutput = configuration.getTailCount() / configuration.getHeadCount();
        if (configuration.getTailCount() % configuration.getHeadCount() != 0) {
            throw new PersistenceException("Incorrect head count: should be a divisor of tail count " + configuration.getTailCount());
        }

        // scan files
        Scanner scanner = new Scanner(objectConfiguration);
        ScanResult scanResult = scanner.scan();

        // prepare pipelines
        Pipeline[] pipelines = Stream.of(scanResult.getElements()).map(Pipeline::new).toArray(Pipeline[]::new);

        // tails
        QueueToPersister[] tailsToUse = new QueueToPersister[pipelines.length];
        for (int i = 0; i < tailsToUse.length; i++) {
            tailsToUse[i] = new QueueToPersister(pipelines[i].getTailCursor());
        }
        tails = tailsToUse;

        // heads
        OutputMultiplexer[] headsToUse = new OutputMultiplexer[configuration.getHeadCount()];
        for (int i = 0; i < headsToUse.length; i++) {
            HeadCursor[] inputs = new HeadCursor[inputsPerOutput];
            for (int k = inputsPerOutput * i; k < inputsPerOutput * (i + 1); k++) {
                inputs[k] = pipelines[k].getHeadCursor();
            }

            headsToUse[i] = new OutputMultiplexer(inputs);
        }
        heads = headsToUse;

        // persister
        persister = new Persister(pipelines, objectConfiguration);
        thread = configuration.getPersisterThreadFactory().newThread(persister);
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

    public void start() {
        persister.loadUncommitted();
        thread.start();
    }

    public void stop() {
        persister.deactivate();
    }
}
