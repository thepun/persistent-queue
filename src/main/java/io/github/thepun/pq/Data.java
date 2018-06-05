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

    }

    long getSequenceId(long cursor) {

    }

    long getCommitSequenceId(long cursor, long length) {

    }

    void sync() {

    }

    void close() {

    }
}
