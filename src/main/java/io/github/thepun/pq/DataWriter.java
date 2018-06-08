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

import io.github.thepun.unsafe.OffHeapMemory;
import io.github.thepun.unsafe.chars.OffHeapCharSequence;

import java.nio.MappedByteBuffer;

final class DataWriter implements WriteBuffer {

    private final long size;
    private final MappedByteBuffer buffer;

    private long cursor;

    DataWriter(Data data) {
        buffer = data.getBuffer();
        size = buffer.capacity();
    }

    @Override
    public void writeByte(byte value) {
        buffer.put(rollover(cursor), value);
        cursor++;
    }

    @Override
    public void writeChar(char value) {
        buffer.putChar(rollover(cursor), value);
        cursor += 2;
    }

    @Override
    public void writeShort(short value) {
        buffer.putShort(rollover(cursor), value);
        cursor += 2;
    }

    @Override
    public void writeInt(int value) {
        buffer.putInt(rollover(cursor), value);
        cursor += 4;
    }

    @Override
    public void writeLong(long value) {
        buffer.putLong(rollover(cursor), value);
        cursor += 8;
    }

    @Override
    public void writeBoolean(boolean value) {
        buffer.put(rollover(cursor), value ? (byte) 1 : (byte) 0);
        cursor++;
    }

    @Override
    public void writeOffHeap(long address, int length) {
        for (int i = 0; i < length; i++) {
            buffer.put(rollover(cursor + i), OffHeapMemory.getByte(address + i));
        }
        cursor += length;
    }

    @Override
    public void writeOffHeap(OffHeapCharSequence offHeapCharSequence) {
        writeOffHeap(offHeapCharSequence.getOffheapAddress(), offHeapCharSequence.getOffheapLength());
    }

    void mark(long sequenceId) {
        align();
        buffer.putLong(rollover(cursor), sequenceId);
        cursor += 8;
    }

    void commit(long sequenceId) {
        align();
        buffer.putLong(rollover(cursor), sequenceId);
        cursor += 8;
    }

    void setCursor(long cursor) {
        this.cursor = cursor;
    }

    long getCursor() {
        return cursor;
    }

    private void align() {
        if (cursor % 8 != 0) {
            cursor = cursor / 8 * 8 + 8;
        }
    }

    private int rollover(long cursor) {
        return (int) (cursor % size);
    }
}
