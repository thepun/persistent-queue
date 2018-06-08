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

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class FileBufferHelper {

    private final FileChannel file;
    private final MappedByteBuffer buffer;

    FileBufferHelper(Path path, int size) throws PersistenceException {
        try {
            file = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new PersistenceException("Failed to open data file: " + path, e);
        }

        try {
            long fileSize = file.size();
            if (fileSize != size) {
                throw new PersistenceException("File " + path + " has wrong size " + fileSize + ". Expected: " + size);
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to get file size: " + path, e);
        }

        try {
            buffer = file.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (IOException e) {
            throw new PersistenceException("Failed to map file to memory: " + size, e);
        }
    }

    FileChannel getFile() {
        return file;
    }

    MappedByteBuffer getBuffer() {
        return buffer;
    }

    void close() {
        // flush data
        buffer.force();

        // close file handle
        try {
            file.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
