/**
 * Copyright (C)2011 - Marat Gariev <thepun599@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;
import sun.misc.Contended;

final class Pipeline extends PipelineMid {

    private long p0, p1, p2, p3, p4, p5, p6, p7;

    Pipeline(ScanResultElement initialScan) {
        super(initialScan);
    }
}

class PipelinePre {

    private long p0, p1, p2, p3, p4, p5;

}

class PipelineMid extends PipelinePre {

    private final Data data;
    private final Sequence sequence;
    private final DataWriter writer;
    private final TailCursor tailCursor;
    private final HeadCursor headCursor;
    private final SerializerCursor serializerCursor;
    private final QueueToPersister queueToPersister;
    private final ScanResultElement initialScan;

    PipelineMid(ScanResultElement initialScan) {
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

        serializerCursor = new SerializerCursor();
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