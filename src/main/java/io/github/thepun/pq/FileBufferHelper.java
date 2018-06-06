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
