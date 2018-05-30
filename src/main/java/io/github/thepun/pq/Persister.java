package io.github.thepun.pq;

import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

final class Persister implements Runnable {

    private final Data data;
    private final Sequence sequence;
    private final Commit[] commits;
    private final ScanResult initialScan;
    private final CountDownLatch finished;
    private final QueueToPersister.Head[] inputs;
    private final QueueFromPersister.Tail[] outputs;
    private final Serializer<Object, Object>[] serializers;
    private final Configuration<Object, Object> configuration;

    private long cursor;
    private boolean stopped;
    private boolean started;

    Persister(QueueToPersister.Head[] inputs, QueueFromPersister.Tail[] outputs, Serializer<Object, Object>[] serializers, Configuration<Object, Object> configuration) throws PersistenceException {
        this.inputs = inputs;
        this.outputs = outputs;
        this.configuration = configuration;
        this.serializers = serializers;

        started = false;
        stopped = false;
        finished = new CountDownLatch(1);

        // scan files and load all
        Scanner scanner = new Scanner(configuration);
        initialScan = scanner.scan();
        data = initialScan.getData();
        sequence = initialScan.getSequence();
        commits = initialScan.getCommits();
    }

    @Override
    public void run() {
        Logger.info("Activating persister");

        synchronized (this) {
            // run only once
            if (started || stopped) {
                return;
            }

            started = true;
        }

        try {
            // prepare everything
            initializeOutputs();
            loadUncommitted();

            // special simplified case when batch sizes are equal and only one input and only one output
            boolean flat = inputs.length == 1 && outputs.length == 1 && configuration.getInputBatchSize() == configuration.getOutputBatchSize();

            // start reading new data
            if (flat) {
                processOneToOne();
            } else {
                processManyToMany();
            }
        } catch (Throwable e) {
            Logger.error(e, "Error during processing of the queue");
        } finally {
            closeFiles();
            finished.countDown();
        }
    }

    void deactivate() {
        Logger.info("Deactivating persister");

        synchronized (this) {
            if (stopped) {
                return;
            }

            stopped = true;

            if (!started) {
                return;
            }
        }

        // wait for executor to finish task
        try {
            finished.await();
        } catch (InterruptedException e) {
            Logger.error(e, "Interrupted during wait for persister to finish");
        }

        Logger.info("Persister fully stopped");
    }

    private void initializeOutputs() {
        Logger.info("Initializing output queues");

        ScanCommitElement[] scannedCommits = initialScan.getScannedCommits();
        for (int i = 0; i < outputs.length; i++) {
            QueueFromPersister.Tail output = outputs[i];
            output.setCommit(commits[i]);
            output.setSequenceId(scannedCommits[i].getSequenceId());
        }
    }

    private void loadUncommitted() {
        Logger.info("Loading uncommitted elements");

        for (int i = 0; i < outputs.length; i++) {




















        }
    }

    private void processOneToOne() {
        // TODO: implement one to one persister
        processManyToMany();
    }

    private void processManyToMany() {
        Logger.info("Processing new elements with multiple I/O");

        PersistCallback<Object, Object> callback = configuration.getPersistCallback();

        Serializer<Object, Object>[] serializersVar = serializers;
        int serializersSize = serializersVar.length;

        int batchReady = 0;
        int inputBatchSize = configuration.getInputBatchSize();
        int outputBatchSize = configuration.getOutputBatchSize();
        Object[] batch = new Object[inputBatchSize];
        int batchLeft = inputBatchSize;

        int inputIndex = 0;
        QueueToPersister.Head[] inputsVar = inputs;
        int inputsSize = inputsVar.length;

        int outputIndex = 0;
        QueueFromPersister.Tail[] outputsVar = outputs;
        int outputsSize = outputsVar.length;

        boolean syncAfterPersist = configuration.isSync();
        boolean notSyncAfterPersist = !syncAfterPersist;
        boolean batchSizeSame = inputBatchSize == outputBatchSize;
        DataWriter dataWriter = data.newWriter(cursor);

        int inputIndexMark = inputsSize;
        for (; ; ) {
            // cancel everything on deactivation
            if (stopped) {
                return;
            }

            // get several available objects from queue
            QueueToPersister.Head input = inputsVar[inputIndex % inputsSize];
            int count = input.get(batch, batchReady, batchLeft);
            batchLeft -= count;
            batchReady += count;

            // if batch is not full 
            if (batchLeft > 0) {
                // check we pulled all inputs
                if (inputIndex == inputIndexMark) {
                    inputIndexMark = inputIndex + inputsSize;
                    inputIndex++;

                    // proceed with batch write only if it is not empty
                    if (batchReady == 0) {
                        continue;
                    }
                } else {
                    // just roll over input
                    inputIndex++;
                    continue;
                }
            }

            // persist objects and invoke callback
            for (int i = 0; i < batchReady; i++) {
                int index = i << 1;
                Object element = batch[index];
                Object elementContext = batch[index | 1];
                int typeHash = element.getClass().hashCode() % serializersSize;
                Serializer<Object, Object> serializer = serializersVar[typeHash];
                serializer.serialize(dataWriter, element, elementContext);
                
                if (notSyncAfterPersist) {
                    callback.onElementPersisted(element, elementContext);
                }
            }

            // sync IO if needed
            if (syncAfterPersist) {
                dataWriter.sync();
                
                // execute callback after sync
                for (int i = 0; i < batchReady; i++) {
                    int index = i << 1;
                    Object element = batch[index];
                    Object elementContext = batch[index | 1];
                    callback.onElementPersisted(element, elementContext);
                }
            }

            // push persisted objects
            if (batchSizeSame) {
                // if input and output batch sizes are equal
                outputsVar[outputIndex % outputsSize].add(batch, batchReady, batchLeft);
                batchReady = 0;
                outputIndex++;
            } else {
                // if output batch size is less then input
                int outputBatchOffset = 0;
                for (; ; ) {
                    if (batchReady > outputBatchSize) {
                        batchReady -= outputBatchSize;
                        outputsVar[outputIndex % outputsSize].add(batch, outputBatchOffset, outputBatchSize);
                        outputBatchOffset += outputBatchSize;
                    } else {
                        outputsVar[outputIndex % outputsSize].add(batch, outputBatchOffset, batchReady);
                        batchReady = 0;
                        break;
                    }

                    outputIndex++;
                }
            }

            // retrigger buffer
            batchLeft = inputBatchSize;
        }
    }

    private void closeFiles() {
        if (data != null) {
            data.close();
        }

        if (sequence != null) {
            sequence.close();
        }

        if (commits != null) {
            Stream.of(commits).forEach(Commit::close);
        }
    }
}
