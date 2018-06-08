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
