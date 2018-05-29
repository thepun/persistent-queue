package io.github.thepun.pq;

import java.nio.MappedByteBuffer;
import java.util.concurrent.CountDownLatch;

final class Persister implements Runnable {

    private final boolean sync;
    private final boolean flat;
    private final int sizeOfInputBatch;
    private final int sizeOfOutputBatch;
    private final CountDownLatch finished;
    private final FileBufferHelper dataBufferHelper;
    private final FileBufferHelper sequenceBufferHelper;
    private final QueueToPersister.Head[] queuesToPersister;
    private final QueueFromPersister.Tail[] queuesFromPersister;
    private final Serializer<Object, Object>[] serializersHastable;
    private final PersistCallback<Object, Object> persistCallback;

    private boolean stopped;
    private boolean started;
    private int initialIndex;
    private int initialDataCursor;
    private int initialSequnceCursor;

    Persister(QueueToPersister.Head[] inputs, QueueFromPersister.Tail[] outputs, Serializer<Object, Object>[] serializers, Configuration<Object, Object> configuration) {
        queuesToPersister = inputs;
        queuesFromPersister = outputs;

        started = false;
        stopped = false;
        finished = new CountDownLatch(1);
        dataBufferHelper = new FileBufferHelper(configuration.getDataPath(), "data", configuration.getDataFileSize());
        sequenceBufferHelper = new FileBufferHelper(configuration.getDataPath(), "sequence", configuration.getSequenceFileSize());

        sync = configuration.isSync();
        serializersHastable = serializers;
        sizeOfInputBatch = configuration.getInputBatchSize();
        sizeOfOutputBatch = configuration.getOutputBatchSize();
        persistCallback = configuration.getPersistCallback();

        // special simplified case when batch sizes are equal and only one input and only one output
        flat = inputs.length == 1 && outputs.length == 1 && sizeOfInputBatch == sizeOfOutputBatch;
    }

    @Override
    public void run() {
        synchronized (this) {
            // run only once
            if (started || stopped) {
                return;
            }

            started = true;
        }

        try {
            // initialize sequence
            findLastSequence();

            // load all uncommited elements
            processUncommitted();

            // start reading new data
            if (flat) {
                processOneToOne();
            } else {
                processManyToMany();
            }
        } finally {
            dataBufferHelper.close();
            sequenceBufferHelper.close();;
            finished.countDown();
        }
    }

    void deactivate() {
        synchronized (this) {
            if (stopped) {
                return;
            }

            stopped = true;

            if (!started) {
                dataBufferHelper.close();
                return;
            }
        }

        // wait for executor to finish task
        try {
            finished.await();
        } catch (InterruptedException e) {
            // just skip
        }
    }

    private void findLastSequence() {

    }

    private void processUncommitted() {

    }

    private void processOneToOne() {
        // TODO: implement one to one persister
        processManyToMany();
    }

    private void processManyToMany() {
        PersistCallback<Object, Object> callback = persistCallback;

        Serializer<Object, Object>[] serializers = serializersHastable;
        int serializersSize = serializers.length;

        int batchReady = 0;
        int inputBatchSize = sizeOfInputBatch;
        Object[] batch = new Object[inputBatchSize];
        int batchLeft = inputBatchSize;

        int inputIndex = 0;
        QueueToPersister.Head[] inputs = queuesToPersister;
        int inputsSize = inputs.length;
        int inputIndexMark = inputsSize;

        int outputIndex = 0;
        int outputBatchSize = sizeOfOutputBatch;
        QueueFromPersister.Tail[] outputs = queuesFromPersister;
        int outputsSize = outputs.length;

        boolean syncAfterPersist = sync;
        boolean notSyncAfterPersist = !syncAfterPersist;
        boolean batchSizeSame = inputBatchSize == outputBatchSize;
        MappedByteBuffer dataBuffer = dataBufferHelper.getBuffer();
        MappedByteBufferWrapper dataBufferWrapper = new MappedByteBufferWrapper(dataBuffer);

        for (; ; ) {
            // cancel everything on deactivation
            if (stopped) {
                return;
            }

            // get several available objects from queue
            QueueToPersister.Head input = inputs[inputIndex % inputsSize];
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
                Serializer<Object, Object> serializer = serializers[typeHash];
                serializer.serialize(dataBufferWrapper, element, elementContext);
                
                if (notSyncAfterPersist) {
                    callback.onElementPersisted(element, elementContext);
                }
            }

            // sync IO if needed
            if (syncAfterPersist) {
                dataBuffer.force();
                
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
                outputs[outputIndex % outputsSize].add(batch, batchReady, batchLeft);
                batchReady = 0;
                outputIndex++;
            } else {
                // if output batch size is less then input
                int outputBatchOffset = 0;
                for (; ; ) {
                    if (batchReady > outputBatchSize) {
                        batchReady -= outputBatchSize;
                        outputs[outputIndex % outputsSize].add(batch, outputBatchOffset, outputBatchSize);
                        outputBatchOffset += outputBatchSize;
                    } else {
                        outputs[outputIndex % outputsSize].add(batch, outputBatchOffset, batchReady);
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
}
