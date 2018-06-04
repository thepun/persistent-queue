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

    ScanResult scan() throws PersistenceException {
        FileSystem fileSystem = FileSystems.getDefault();

        // check root directory
        Path rootPath = fileSystem.getPath(configuration.getDataPath()).toAbsolutePath();
        if (!Files.exists(rootPath)) {
            throw new PersistenceException("Data path not found: " + rootPath);
        }

        ScanResultElement[] elements = new ScanResultElement[configuration.getTailCount()];
        for (int inputIndex = 0; inputIndex < configuration.getTailCount(); inputIndex++) {
            ScanResultElement element = scanElement(rootPath, inputIndex);
            elements[inputIndex] = element;
        }
    }

    private ScanResultElement initialElement(Path rootPath, int inputIndex) {
        clearElementFiles(rootPath, inputIndex);


    }

    private ScanResultElement scanElement(Path rootPath, int inputIndex) throws PersistenceException {
        ScanResultElement element = new ScanResultElement();

        // check sequence file
        Path sequencePath = rootPath.resolveSibling("sequence_" + inputIndex);
        if (!Files.exists(sequencePath)) {
            return initialElement(rootPath, inputIndex);
        }

        // check data file
        Path dataPath = rootPath.resolveSibling("data_" + inputIndex);

        // initial files
        FileBufferHelper dataBufferHelper = new FileBufferHelper(dataPath, configuration.getDataFileSize());
        Data data = new Data(dataBufferHelper);

        // scan sequence
        FileBufferHelper sequenceBufferHelper = new FileBufferHelper(sequencePath, configuration.getSequenceFileSize());
        Sequence sequence = new Sequence(sequenceBufferHelper);
        Commit commit = new Commit(sequenceBufferHelper);

        long lastId = -1;
        long lastDataCursor = 0;
        long lastDataLength = 0;
        long lastSequenceCursor = 0;
        sequence.initial();
        for (int i = 0; i < sequence.getEntriesCount(); i ++) {
            if (sequence.getId() > lastId) {
                lastId = sequence.getId();
                lastDataCursor = sequence.getElementCursor();
                lastDataLength = sequence.getElementLength();
                lastSequenceCursor = i;
            } else if (sequence.getId() == 0) {
                break;
            }
        }

        initialSequnceId = lastId;
        initialSequnceCursor = lastSequenceCursor;
        initialDataCursor = lastDataCursor;
        initialDataLength = lastDataLength;

        return element;
    }

    private void clearElementFiles(Path rootPath, int inputIndex) {

    }
}
