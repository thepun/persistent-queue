package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;
import io.github.thepun.unsafe.ObjectMemory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

final class Persister implements Runnable {

    private static final long STOPPED_FIELD_OFFSET = ObjectMemory.fieldOffset(Persister.class, "stopped");


    private final Pipeline[] pipelines;
    private final CountDownLatch finished;
    private final PersistCallback<Object, Object> callback;

    private final int[] marshallerIdByClass;
    private final Marshaller<Object, Object>[] marshallersById;
    private final Marshaller<Object, Object>[] marshallersByClass;

    private int stopped;
    private int started;

    Persister(Pipeline[] pipelines, Configuration<Object, Object> configuration) {
        this.pipelines = pipelines;

        finished = new CountDownLatch(1);
        started = 0;
        stopped = 0;

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

        callback = configuration.getPersistCallback();
    }

    @Override
    public void run() {
        Logger.info("Activating persister");

        synchronized (this) {
            // run only once
            if (started == 1 || stopped == 1) {
                return;
            }

            started = 1;
        }

        try {
            processPipelines();
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
            if (stopped == 1) {
                return;
            }

            stopped = 1;

            if (started == 0) {
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

    void loadUncommitted() {
        Logger.info("Loading uncommitted elements");

        for (Pipeline pipeline : pipelines) {
            Sequence sequence = pipeline.getSequence();
            ScanResultElement initialScan = pipeline.getInitialScan();
            SerializerCursor serializerCursor = pipeline.getSerializerCursor();

            if (initialScan.isUncommittedData()) {
                Logger.warn("Found uncommitted data in queue {}", initialScan.getId());

                sequence.setCursor(initialScan.getMinAvailableUncommittedSequenceCursor());

                DataReader reader = new DataReader(pipeline.getData());
                do {
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
                    reader.setCursorAndSkipId(sequence.getElementCursor());
                    reader.setLimitWithoutCommit(sequence.getElementCursor() + sequence.getElementLength());
                    Object element;
                    try {
                        element = marshaller.deserialize(reader);
                    } catch (Throwable e) {
                        Logger.error(e, "Error during unmarshalling element at {} of type {}", sequence.getElementCursor(), elementType);
                        sequence.next();
                        continue;
                    }

                    // put element to batch
                    if (element == null) {
                        Logger.error("Null unmarshalled element at {} of type {}", sequence.getElementCursor(), elementType);
                        sequence.next();
                        continue;
                    }

                    // push unmarshalled objects to queue
                    pipeline.getQueueToPersister().add(element, null);
                    sequence.next();
                } while (sequence.getId() > 0 && sequence.getId() <= initialScan.getMaxAvailableUncommittedSequenceId());

                // repair cursors
                TailCursor tailCursor = pipeline.getTailCursor();
                serializerCursor.setCursor(tailCursor.getCursor());
                serializerCursor.setNodeIndex(tailCursor.getNodeIndex());
                serializerCursor.setCurrentNode(tailCursor.getCurrentNode());
            }

            // sequence id to start
            serializerCursor.setNextSequenceId(initialScan.getSequenceId() + 1);
            sequence.setCursor(initialScan.getSequenceCursor());
        }
    }

    private void processPipelines() {
        Logger.info("Processing new elements with multiple I/O");

        Marshaller<Object, Object>[] marshallers = marshallersByClass;
        int serializersSize = marshallers.length;

        long inputIndex = 0;
        Pipeline[] pipelinesVar = pipelines;
        int pipelinesSize = pipelinesVar.length;

        int[] marshallerIds = marshallerIdByClass;
        PersistCallback<Object, Object> callbackVar = callback;

        inputLoop:
        for (; ; ) {
            Pipeline pipeline = pipelinesVar[(int) (inputIndex % pipelinesSize)];
            Sequence sequence = pipeline.getSequence();
            DataWriter writer = pipeline.getWriter();
            SerializerCursor input = pipeline.getSerializerCursor();
            Object[] currentNodeVar = input.getCurrentNode();
            long readIndexVar = input.getCursor();
            long nodeIndexVar = input.getNodeIndex();
            long nextSequenceId = input.getNextSequenceId();

            for (;;) {
                // cancel everything on deactivation
                // we ensure that it will not be inlined and always requesting memory
                if (ObjectMemory.getInt(this, STOPPED_FIELD_OFFSET) == 1) {
                    return;
                }

                // check we need to move to another node
                long elementNodeIndex = readIndexVar >> Node.NODE_DATA_SHIFT;
                if (elementNodeIndex != nodeIndexVar) {
                    // try get next node from chain
                    Object[] nextNode = Node.getNext(currentNodeVar);
                    if (nextNode == null) {
                        inputIndex++;
                        continue inputLoop;
                    }

                    MemoryFence.load();

                    // use new node as current
                    currentNodeVar = nextNode;
                    nodeIndexVar = elementNodeIndex;
                    input.setNodeIndex(elementNodeIndex);
                    input.setCurrentNode(currentNodeVar);
                }

                int elementIndex = (int) (readIndexVar & Node.NODE_DATA_SIZE_MASK);
                Object element = Node.getElement(currentNodeVar, elementIndex);
                if (element == null) {
                    // another thread didn't write to the index yet
                    inputIndex++;
                    continue inputLoop;
                }

                MemoryFence.load();

                // persist object and invoke callback
                Object elementContext = Node.getElement(currentNodeVar, elementIndex | 1);
                int typeHash = element.getClass().hashCode() % serializersSize;
                Marshaller<Object, Object> marshaller = marshallers[typeHash];

                // write to data file
                long initialDataCursor = writer.getCursor();
                writer.mark(nextSequenceId);
                try {
                    marshaller.serialize(writer, element, elementContext);
                } catch (Throwable e) {
                    Logger.error(e, "Error during marshalling element {}", element);
                    writer.setCursor(initialDataCursor);
                    continue;
                }
                writer.commit(nextSequenceId);

                // write to sequence file
                sequence.setId(nextSequenceId);
                sequence.setElementCursor(initialDataCursor);
                sequence.setElementLength((int)(writer.getCursor() - initialDataCursor));
                sequence.setElementType(marshallerIds[typeHash]);
                sequence.commit(nextSequenceId);
                sequence.next();

                // increment sequence
                nextSequenceId++;
                input.setNextSequenceId(nextSequenceId);

                // execute callback
                callbackVar.onElementPersisted(element, elementContext);

                // counters for next step
                readIndexVar += 2;
                MemoryFence.store(); // increment cursor only after all data is saved
                input.setCursor(readIndexVar);
            }
        }
    }

    private void closeFiles() {
        for (Pipeline pipeline : pipelines) {
            pipeline.getData().close();
            pipeline.getSequence().close();
        }
    }
}
