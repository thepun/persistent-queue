package io.github.thepun.pq;

import java.nio.MappedByteBuffer;

final class Sequence {

    private static final int SEQUENCE_ELEMENT_SIZE = 32;
    private static final int SEQUENCE_ID_OFFSET = 0;
    private static final int SEQUENCE_OUTPUT_OFFSET = 8;
    private static final int SEQUENCE_ELEMENT_CURSOR_OFFSET = 16;
    private static final int SEQUENCE_ELEMENT_LENGTH_OFFSET = 24;


    private final FileBufferHelper helper;
    private final MappedByteBuffer buffer;

    Sequence(FileBufferHelper helper) {
        this.helper = helper;

        buffer = helper.getBuffer();
    }

    long getCursor() {

    }

    void setCursor(long cursor) {

    }

    void previous() {

    }

    long getId() {

    }

    long getCommitId() {

    }

    void setId(long id) {

    }

    int getElementType() {

    }

    void setElementType(int elementType) {

    }


    long getElementCursor() {

    }

    void setElementCursor(long elementCursor) {

    }

    long getElementLength() {

    }

    void setElementLength(long elementLength) {

    }

    long getNextElementCursor() {

    }

    long getNextId() {

    }

    int getEntriesCount() {

    }

    void initial() {

    }

    void next() {

    }

    void prev() {

    }

    void commit(long sequenceId) {

    }

    long lastCommitted() {

    }

    void markLastCommitted(long sequenceId) {

    }

    void sync() {

    }

    void close() {

    }
}
