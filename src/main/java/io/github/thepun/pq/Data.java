package io.github.thepun.pq;

import java.nio.MappedByteBuffer;

final class Data {

    private final FileBufferHelper helper;
    private final MappedByteBuffer buffer;

    Data(FileBufferHelper helper) {
        this.helper = helper;

        buffer = helper.getBuffer();
    }

    MappedByteBuffer getBuffer() {
        return buffer;
    }

    long getSequenceId(long cursor) {
        return buffer.getLong(align(cursor));
    }

    long getCommitSequenceId(long cursor, long length) {
        return buffer.getLong(align(cursor + length - 8));
    }

    void sync() {
        buffer.force();
    }

    void close() {
        helper.close();
    }

    private int align(long cursor) {
        if (cursor % 8 != 0) {
            cursor = cursor / 8 * 8 + 8;
        }

        return (int) cursor;
    }
}
