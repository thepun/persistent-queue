package io.github.thepun.pq;

import java.nio.MappedByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

final class Scanner {

    private final Configuration<Object, Object> configuration;

    Scanner(Configuration<Object, Object> configuration) {
        this.configuration = configuration;
    }

    ScanResult scan() {
        FileSystem fileSystem = FileSystems.getDefault();

        // check root directory
        Path rootPath = fileSystem.getPath(dataPath).toAbsolutePath();
        if (!Files.exists(rootPath)) {
            throw new PersistenceException("Data path not found: " + rootPath);
        }

        // check sequence file
        Path sequencePath = rootPath.resolveSibling("sequence");
        if (!Files.exists(sequencePath)) {
            return initial(rootPath);
        }

        // check data file
        Path dataPath = rootPath.resolveSibling("data");

        // initial files
        FileBufferHelper dataBufferHelper = new FileBufferHelper(configuration.getDataPath(), "data", configuration.getDataFileSize());
        FileBufferHelper sequenceBufferHelper = new FileBufferHelper(configuration.getDataPath(), "sequence", configuration.getSequenceFileSize());

        // scan for commit filea


        FileBufferHelper[] commitBufferHelpers = new FileBufferHelper[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            commitBufferHelpers[i] = new FileBufferHelper(configuration.getDataPath(), "commit_" + i, configuration.getSequenceFileSize());
        }


        MappedByteBuffer buffer = sequenceBufferHelper.getBuffer();

        long lastId = -1;
        long lastDataCursor = 0;
        long lastDataLength = 0;
        long lastSequenceCursor = 0;
        for (int i = 0; i < sizeOfSequence; i += SEQUENCE_ELEMENT_SIZE) {
            long id = buffer.getLong(i + SEQUENCE_ID_OFFSET);
            long cursor = buffer.getLong(i + SEQUENCE_CURSOR_OFFSET);
            long length = buffer.getLong(i + SEQUENCE_LENGTH_OFFSET);
            if (id > lastId) {
                lastId = id;
                lastDataCursor = cursor;
                lastDataLength = length;
                lastSequenceCursor = i;
            } else if (id == 0) {
                break;
            }
        }

        initialSequnceId = lastId;
        initialSequnceCursor = lastSequenceCursor;
        initialDataCursor = lastDataCursor;
        initialDataLength = lastDataLength;
    }

    private ScanResult initial(Path rootPath) {
        clearAllFiles(rootPath);


    }

    private void clearAllFiles(Path rootPath) {

    }
}
