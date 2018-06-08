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

import java.nio.MappedByteBuffer;

final class Sequence {

    private static final int SEQUENCE_OFFSET = 8;
    private static final int SEQUENCE_ITEM_SIZE = 32;
    private static final int SEQUENCE_ID_OFFSET = 0;
    private static final int SEQUENCE_ELEMENT_CURSOR_OFFSET = 8;
    private static final int SEQUENCE_ELEMENT_LENGTH_OFFSET = 16;
    private static final int SEQUENCE_ELEMENT_TYPE_OFFSET = 20;
    private static final int SEQUENCE_COMMIT_OFFSET = 24;


    private final long seqSize;
    private final FileBufferHelper helper;
    private final MappedByteBuffer buffer;

    private long cursor;

    Sequence(FileBufferHelper helper) {
        this.helper = helper;

        buffer = helper.getBuffer();
        seqSize = buffer.capacity() - SEQUENCE_OFFSET;
        cursor = SEQUENCE_OFFSET;
    }

    long getCursor() {
        return cursor;
    }

    void setCursor(long cursor) {
        this.cursor = cursor;
    }

    long getCommitId() {
        return buffer.getLong(align(cursor) + SEQUENCE_COMMIT_OFFSET);
    }

    long getId() {
        return buffer.getLong(align(cursor) + SEQUENCE_ID_OFFSET);
    }

    void setId(long id) {
        buffer.putLong(align(cursor) + SEQUENCE_ID_OFFSET, id);
    }

    int getElementType() {
        return buffer.getInt(align(cursor) + SEQUENCE_ELEMENT_TYPE_OFFSET);
    }

    void setElementType(int elementType) {
        buffer.putInt(align(cursor) + SEQUENCE_ELEMENT_TYPE_OFFSET, elementType);
    }

    long getElementCursor() {
        return buffer.getLong(align(cursor) + SEQUENCE_ELEMENT_CURSOR_OFFSET);
    }

    void setElementCursor(long elementCursor) {
        buffer.putLong(align(cursor) + SEQUENCE_ELEMENT_CURSOR_OFFSET, elementCursor);
    }

    int getElementLength() {
        return buffer.getInt(align(cursor) + SEQUENCE_ELEMENT_LENGTH_OFFSET);
    }

    void setElementLength(int elementLength) {
        buffer.putInt(align(cursor) + SEQUENCE_ELEMENT_LENGTH_OFFSET, elementLength);
    }

    int getEntriesCount() {
        return (buffer.capacity() - SEQUENCE_OFFSET) / SEQUENCE_ITEM_SIZE;
    }

    void initial() {
        cursor = SEQUENCE_OFFSET;
    }

    void next() {
        long newCursor = cursor + SEQUENCE_ITEM_SIZE;
        cursor = rotate(newCursor, seqSize);
    }

    void prev() {
        long newCursor = cursor - SEQUENCE_ITEM_SIZE;
        cursor = rotate(newCursor, seqSize);
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

    private static long rotate(long cursor, long seqSize) {
        return (cursor - SEQUENCE_OFFSET) % seqSize + SEQUENCE_OFFSET;
    }

    private static int align(long cursor) {
        return (int) cursor;
    }
}
