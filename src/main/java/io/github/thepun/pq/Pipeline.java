package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;
import sun.misc.Contended;

@Contended
final class Pipeline {

    private final Data data;
    private final Sequence sequence;
    private final DataWriter writer;
    private final TailCursor tailCursor;
    private final HeadCursor headCursor;
    private final SerializerCursor serializerCursor;
    private final QueueToPersister queueToPersister;
    private final ScanResultElement initialScan;

    Pipeline(ScanResultElement initialScan) {
        this.initialScan = initialScan;

        data = initialScan.getData();
        sequence = initialScan.getSequence();
        writer = new DataWriter(data);
        writer.setCursor(initialScan.getDataCursor());

        Object[] currentNode = Node.createNew();
        Object[] freeNode = Node.createNew();

        tailCursor = new TailCursor();
        tailCursor.setCurrentNode(currentNode);
        tailCursor.setFreeNode(freeNode);

        serializerCursor = new SerializerCursor(tailCursor);
        serializerCursor.setCurrentNode(currentNode);

        headCursor = new HeadCursor(serializerCursor);
        headCursor.setData(data);
        headCursor.setSequence(sequence);
        headCursor.setCurrentNode(currentNode);
        headCursor.setFreeNode(freeNode);

        queueToPersister = new QueueToPersister(tailCursor);

        MemoryFence.full();
    }

    ScanResultElement getInitialScan() {
        return initialScan;
    }

    DataWriter getWriter() {
        return writer;
    }

    Data getData() {
        return data;
    }

    Sequence getSequence() {
        return sequence;
    }

    QueueToPersister getQueueToPersister() {
        return queueToPersister;
    }

    TailCursor getTailCursor() {
        return tailCursor;
    }

    HeadCursor getHeadCursor() {
        return headCursor;
    }

    SerializerCursor getSerializerCursor() {
        return serializerCursor;
    }
}
