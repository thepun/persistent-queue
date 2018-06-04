package io.github.thepun.pq;

import java.nio.MappedByteBuffer;

final class Data {

    private final FileBufferHelper helper;
    private final MappedByteBuffer buffer;

    Data(FileBufferHelper helper) {
        this.helper = helper;

        buffer = helper.getBuffer();
    }

    DataWriter newWriter() {

    }

    DataReader newReader() {

    }

    void sync() {

    }

    void close() {

    }
}
