package io.github.thepun.pq;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class FileBufferHelper {

    private final FileChannel file;
    private final MappedByteBuffer buffer;

    FileBufferHelper(String parentPath, String fileName, int size) {
        Path path = FileSystems.getDefault().getPath(parentPath, fileName);

        try {
            file = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new PersistenceException("Failed to open data file: " + path, e);
        }

        try {
            buffer = file.map(FileChannel.MapMode.PRIVATE, 0, size);
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
        try {
            file.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
