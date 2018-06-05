package io.github.thepun.pq;

import java.nio.MappedByteBuffer;

final class Sequence {

    private static final int SEQUENCE_OFFSET = 8;
    private static final int SEQUENCE_ITEM_SIZE = 32;
    private static final int SEQUENCE_ID_OFFSET = 0;
    private static final int SEQUENCE_ELEMENT_CURSOR_OFFSET = 8;
    private static final int SEQUENCE_ELEMENT_LENGTH_OFFSET = 16;
    private static final int SEQUENCE_ELEMENT_TYPE_OFFSET = 20;
    private static final int SEQUENCE_COMMIT_OFFSET = 24;


    private final FileBufferHelper helper;
    private final MappedByteBuffer buffer;

    private long cursor;

    Sequence(FileBufferHelper helper) {
        this.helper = helper;

        buffer = helper.getBuffer();
    }

    long getCursor() {
        return cursor;
    }

    void setCursor(long cursor) {
        this.cursor = cursor;
    }

    long getCommitId() {
        return buffer.getLong((int) (cursor + SEQUENCE_COMMIT_OFFSET));
    }

    long getId() {
        return buffer.getLong((int) (cursor + SEQUENCE_ID_OFFSET));
    }

    void setId(long id) {
        buffer.putLong((int) (cursor + SEQUENCE_ID_OFFSET), id);
    }

    int getElementType() {
        return buffer.getInt((int) (cursor + SEQUENCE_ELEMENT_TYPE_OFFSET));
    }

    void setElementType(int elementType) {
        buffer.putInt((int) (cursor + SEQUENCE_ELEMENT_TYPE_OFFSET), elementType);
    }

    long getElementCursor() {
        return buffer.getLong((int) (cursor + SEQUENCE_ELEMENT_CURSOR_OFFSET));
    }

    void setElementCursor(long elementCursor) {
        buffer.putLong((int) (cursor + SEQUENCE_ELEMENT_CURSOR_OFFSET), elementCursor);
    }

    int getElementLength() {
        return buffer.getInt((int) (cursor + SEQUENCE_ELEMENT_LENGTH_OFFSET));
    }

    void setElementLength(int elementLength) {
        buffer.putInt((int) (cursor + SEQUENCE_ELEMENT_LENGTH_OFFSET), elementLength);
    }

    int getEntriesCount() {
        return (buffer.capacity() - SEQUENCE_OFFSET) / SEQUENCE_ITEM_SIZE;
    }

    void initial() {
        cursor = SEQUENCE_OFFSET;
    }

    void next() {
        cursor += SEQUENCE_ITEM_SIZE;
    }

    void prev() {
        cursor -= SEQUENCE_ITEM_SIZE;
    }

    void commit(long sequenceId) {
        buffer.putLong((int) (cursor + SEQUENCE_COMMIT_OFFSET), sequenceId);
    }

    long lastCommitted() {
        return buffer.getLong(0);
    }

    void markLastCommitted(long sequenceId) {
        buffer.putLong(0, sequenceId);
    }

    void sync() {
        buffer.force();
    }

    void close() {
        helper.close();
    }
}
