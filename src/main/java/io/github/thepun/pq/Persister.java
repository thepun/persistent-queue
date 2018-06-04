package io.github.thepun.pq;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

final class Persister implements Runnable {

    private final Data data;
    private final Sequence sequence;
    private final Commit[] commits;
    private final ScanResult scanResult;
    private final CountDownLatch finished;
    private final Pipeline.Head[] inputs;
    private final BufferedQueueFromPersister.Tail[] outputs;
    private final Configuration<Object, Object> configuration;
    private final Marshaller<Object, Object>[] marshallersById;
    private final Marshaller<Object, Object>[] marshallersByClass;
    private final int[] marshallerIdByClass;

    private boolean stopped;
    private boolean started;

    Persister(Pipeline.Head[] inputs, BufferedQueueFromPersister.Tail[] outputs, ScanResult scanResult, Configuration<Object, Object> configuration) throws PersistenceException {
        this.inputs = inputs;
        this.outputs = outputs;
        this.scanResult = scanResult;
        this.configuration = configuration;

        data = scanResult.getData();
        sequence = scanResult.getSequence();
        commits = scanResult.getCommits();
        finished = new CountDownLatch(1);
        started = false;
        stopped = false;

        Map<Class<?>, Marshaller<?, ?>> serializersMap = configuration.getSerializers();

        // prepare marshallers by class
        int size1 = MathUtil.nextGreaterPrime(serializersMap.size());
        int[] localMarshallerIdsByClass = new int[size1];
        Marshaller<Object, Object>[] localMarshallersByClass = new Marshaller[size1];
        upperLoop:
        for (; ; ) {
            for (Map.Entry<Class<?>, Marshaller<?, ?>> entry : serializersMap.entrySet()) {
                Class<?> type = entry.getKey();
                int typeHash = type.hashCode() % size1;

                // if we found element with same hash restart with grather hash table size
                Marshaller<?, ?> anotherMarshaller = localMarshallersByClass[typeHash];
                if (anotherMarshaller != null) {
                    size1 = MathUtil.nextGreaterPrime(size1);
                    localMarshallerIdsByClass = new int[size1];
                    localMarshallersByClass = new Marshaller[size1];
                    continue upperLoop;
                }

                Marshaller<Object, Object> marshaller = (Marshaller<Object, Object>) entry.getValue();
                localMarshallersByClass[typeHash] = marshaller;
                localMarshallerIdsByClass[typeHash] = marshaller.getTypeId();
            }

            break;
        }
        marshallersByClass = localMarshallersByClass;
        marshallerIdByClass = localMarshallerIdsByClass;

        // prepare marshallers by type id
        int size2 = MathUtil.nextGreaterPrime(serializersMap.size());
        Marshaller<Object, Object>[] localMarshallersById = new Marshaller[size2];
        upperLoop:
        for (; ; ) {
            for (Map.Entry<Class<?>, Marshaller<?, ?>> entry : serializersMap.entrySet()) {
                int typeHash = entry.getValue().getTypeId() % size2;

                // if we found element with same hash restart with grather hash table size
                Marshaller<?, ?> anotherMarshaller = localMarshallersById[typeHash];
                if (anotherMarshaller != null) {
                    size2 = MathUtil.nextGreaterPrime(size2);
                    localMarshallersById = new Marshaller[size2];
                    continue upperLoop;
                }

                localMarshallersById[typeHash] = (Marshaller<Object, Object>) entry.getValue();
            }

            break;
        }
        marshallersById = localMarshallersById;
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

        ScanCommitElement[] scannedCommits = scanResult.getScannedCommits();
        for (int i = 0; i < outputs.length; i++) {
            BufferedQueueFromPersister.Tail output = outputs[i];
            output.setData(data);
            output.setSequence(sequence);
            output.setCommit(commits[i]);
            output.setSequenceId(scannedCommits[i].getSequenceId());
        }

        // initialize sequence to latest consistent element
        sequence.setCursor(scanResult.getMaxAvailableSequnceCursor());
    }

    private void loadUncommitted() {
        Logger.info("Loading uncommitted elements");

        Object[] batch = null;
        DataReader reader = null;

        ScanCommitElement[] scannedCommits = scanResult.getScannedCommits();
        for (int outputIndex = 0; outputIndex < outputs.length; outputIndex++) {
            long initialSequenceCursor = sequence.getCursor();

            ScanCommitElement scannedCommit = scannedCommits[outputIndex];
            if (scannedCommit.isUncommittedData()) {
                Logger.warn("Found uncommitted data in queue {}", outputIndex);

                if (reader == null) {
                    reader = data.newReader();
                }
                if (batch == null) {
                    batch = new Object[configuration.getOutputBatchSize()];
                }

                sequence.setCursor(scannedCommit.getMinAvailableUncommittedSequenceCursor());

                int batchIndex = 0;
                do {
                    // skip elements from another output
                    if (sequence.getOutput() != outputIndex) {
                        continue;
                    }

                    // try to fin marshaller for current element
                    int elementType = sequence.getElementType();
                    int typeHash = elementType % marshallersById.length;
                    Marshaller<Object, Object> marshaller = marshallersById[typeHash];
                    if (marshaller == null || marshaller.getTypeId() != elementType) {
                        Logger.error("Failed to find marshaller for type with id {}", elementType);
                        sequence.next();
                        continue;
                    }

                    // read element from data file
                    reader.setCursor(sequence.getElementCursor());
                    reader.setLimit(sequence.getElementLength());
                    Object element;
                    try {
                        element = marshaller.deserialize(reader);
                    } catch (Throwable e) {
                        Logger.error(e, "Error during unmarshalling element at {} of type {}", sequence.getElementCursor(), elementType);
                        sequence.next();
                        continue;
                    }

                    // put element to batch
                    if (element != null) {
                        batch[batchIndex++] = element;
                    } else {
                        Logger.error("Null unmarshalled element at {} of type {}", sequence.getElementCursor(), elementType);
                    }

                    // push unmarshalled objects to queue if batch is full
                    if (batchIndex == batch.length) {
                        int done = 0;
                        int rest = batch.length;
                        do {
                            int size = outputs[outputIndex].add(batch, done, rest);
                            done += size;
                            rest += size;
                        } while (rest > 0);
                    }

                    sequence.next();
                } while (sequence.getId() <= scannedCommit.getMaxAvailableUncommittedSequenceId());

                // push rest unmarshalled objects to queue
                if (batchIndex > 0) {
                    outputs[outputIndex].add(batch, 0, batchIndex);
                }

                // restore sequence
                sequence.setCursor(initialSequenceCursor);
            }
        }
    }

    private void processOneToOne() {
        // TODO: implement one to one persister
        processManyToMany();
    }

    private void processManyToMany() {
        Logger.info("Processing new elements with multiple I/O");

        PersistCallback<Object, Object> callback = configuration.getPersistCallback();

        Data dataVar = data;
        Sequence sequenceVar = sequence;
        DataWriter writer = dataVar.newWriter();
        writer.setCursor(sequenceVar.getNextElementCursor());
        long sequenceId = scanResult.getMaxAvailableSequnceId() + 1;

        Marshaller<Object, Object>[] marshallers = marshallersByClass;
        int serializersSize = marshallers.length;

        int batchReady = 0;
        int inputBatchSize = configuration.getInputBatchSize();
        int outputBatchSize = configuration.getOutputBatchSize();
        Object[] batch = new Object[inputBatchSize];
        int batchLeft = inputBatchSize;

        int inputIndex = 0;
        Pipeline.Head[] inputsVar = inputs;
        int inputsSize = inputsVar.length;

        int outputIndex = 0;
        BufferedQueueFromPersister.Tail[] outputsVar = outputs;
        int outputsSize = outputsVar.length;

        int inputIndexMark = inputsSize;
        for (; ; ) {
            // cancel everything on deactivation
            if (stopped) {
                return;
            }

            // get several available objects from queue
            Pipeline.Head input = inputsVar[inputIndex % inputsSize];
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
                Marshaller<Object, Object> marshaller = marshallers[typeHash];

                // write to data file
                long initialDataCursor = writer.getCursor();
                writer.mark(sequenceId);
                try {
                    marshaller.serialize(writer, element, elementContext);
                } catch (Throwable e) {
                    Logger.error(e, "Error during marshalling element {}", element);
                    writer.setCursor(initialDataCursor);
                    continue;
                }
                writer.mark(sequenceId);

                // write to sequence file
                sequenceVar.next();
                sequenceVar.setId(sequenceId);
                sequenceVar.setOutput(outputIndex);
                sequenceVar.setElementCursor(initialDataCursor);
                sequenceVar.setElementLength(writer.getCursor() - initialDataCursor);
                sequenceVar.setElementType(marshallerIdByClass[typeHash]);
                sequenceVar.commit();
                sequenceId++;

                // execute callback
                callback.onElementPersisted(element, elementContext);
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
