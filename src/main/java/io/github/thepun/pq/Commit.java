package io.github.thepun.pq;

import java.nio.MappedByteBuffer;

final class Commit {

    private final FileBufferHelper helper;
    private final MappedByteBuffer buffer;

    Commit(FileBufferHelper helper) {
        this.helper = helper;

        buffer = helper.getBuffer();
    }

    void mark(long sequenceId) {

    }

    void sync() {

    }

    void close() {

    }

}
