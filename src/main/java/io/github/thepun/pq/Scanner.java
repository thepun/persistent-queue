package io.github.thepun.pq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

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

        ScanResult result = new ScanResult();
        result.setElements(elements);
        return result;
    }

    private ScanResultElement initialElement(Path rootPath, int inputIndex) throws PersistenceException {
        ScanResultElement element = new ScanResultElement();
        element.setInitial(true);
        element.setSequenceId(0);
        element.setCommitedSequenceId(0);
        element.setUncommittedData(false);
        element.setMinAvailableUncommittedSequenceId(0);
        element.setMinAvailableUncommittedSequenceCursor(-1);
        element.setMaxAvailableUncommittedSequenceId(0);
        element.setMaxAvailableUncommittedSequenceCursor(-1);

        // sequence file
        Path sequencePath = rootPath.resolve("sequence_" + inputIndex);
        if (Files.exists(sequencePath)) {
            throw new PersistenceException("File " + sequencePath + " already exists");
        }
        createFile(sequencePath, configuration.getSequenceFileSize());
        FileBufferHelper sequenceBufferHelper = new FileBufferHelper(sequencePath, configuration.getSequenceFileSize());
        Sequence sequence = new Sequence(sequenceBufferHelper);
        element.setSequence(sequence);
        element.setSequenceCursor(sequence.getCursor());

        // data file
        Path dataPath = rootPath.resolve("data_" + inputIndex);
        if (Files.exists(dataPath)) {
            throw new PersistenceException("File " + dataPath + " already exists");
        }
        createFile(dataPath, configuration.getDataFileSize());
        FileBufferHelper dataBufferHelper = new FileBufferHelper(dataPath, configuration.getDataFileSize());
        Data data = new Data(dataBufferHelper);
        element.setData(data);
        element.setDataCursor(0);

        return element;
    }

    private void createFile(Path path, int size) throws PersistenceException {
        try {
            FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE);
            fileChannel.position(size - 1);
            fileChannel.write(ByteBuffer.allocate(1));
            fileChannel.force(true);
            fileChannel.close();
        } catch (IOException e) {
            throw new PersistenceException("Failed to create file " + path, e);
        }
    }

    private ScanResultElement scanElement(Path rootPath, int inputIndex) throws PersistenceException {
        ScanResultElement element = new ScanResultElement();
        element.setInitial(false);
        element.setUncommittedData(false);
        element.setMinAvailableUncommittedSequenceId(0);
        element.setMinAvailableUncommittedSequenceCursor(-1);
        element.setMaxAvailableUncommittedSequenceId(0);
        element.setMaxAvailableUncommittedSequenceCursor(-1);

        // sequence file
        Path sequencePath = rootPath.resolve("sequence_" + inputIndex);
        if (!Files.exists(sequencePath)) {
            Logger.warn("Sequence file {} was not found", sequencePath);
            return initialElement(rootPath, inputIndex);
        }
        FileBufferHelper sequenceBufferHelper = new FileBufferHelper(sequencePath, configuration.getSequenceFileSize());
        Sequence sequence = new Sequence(sequenceBufferHelper);
        element.setSequence(sequence);

        // data file
        Path dataPath = rootPath.resolve("data_" + inputIndex);
        if (!Files.exists(dataPath)) {
            Logger.warn("Data file {} was not found", sequencePath);
            return initialElement(rootPath, inputIndex);
        }
        FileBufferHelper dataBufferHelper = new FileBufferHelper(dataPath, configuration.getDataFileSize());
        Data data = new Data(dataBufferHelper);
        element.setData(data);

        // max id
        long maxSequenceCursor = findMaxSequence(sequence, data);
        if (maxSequenceCursor == -1) {
            return initialElement(rootPath, inputIndex);
        }
        sequence.setCursor(maxSequenceCursor);
        long maxSequenceId = sequence.getId();
        element.setMaxAvailableSequenceId(maxSequenceId);
        element.setMaxAvailableSequenceCursor(maxSequenceCursor);
        element.setDataCursor(sequence.getElementCursor() + sequence.getElementLength());

        // min id
        long minSequenceCursor = findMinSequenceBack(sequence, data);
        sequence.setCursor(minSequenceCursor);
        long minSequenceId = sequence.getId();
        element.setMinAvailableSequenceId(minSequenceId);
        element.setMinAvailableSequenceCursor(minSequenceCursor);

        // committed
        long committedSequenceId = sequence.lastCommitted();
        if (committedSequenceId < maxSequenceId) {
            element.setUncommittedData(true);
            element.setMaxAvailableUncommittedSequenceId(maxSequenceId);
            element.setMaxAvailableUncommittedSequenceCursor(maxSequenceCursor);
            if (committedSequenceId + 1 >= minSequenceId) {
                element.setCommitedSequenceId(committedSequenceId);
                element.setMinAvailableUncommittedSequenceId(committedSequenceId + 1);
                element.setMinAvailableUncommittedSequenceCursor(findCursor(sequence, committedSequenceId + 1));
            } else {
                element.setCommitedSequenceId(minSequenceId);
                element.setMinAvailableUncommittedSequenceId(minSequenceId);
                element.setMinAvailableUncommittedSequenceCursor(minSequenceCursor);
                Logger.warn("Committed sequence id is less then min available");
            }
        } else {
            element.setCommitedSequenceId(maxSequenceId);
            Logger.warn("Committed sequence id is greater then max available");
        }

        return element;
    }

    private long findCursor(Sequence sequence, long sequenceId) {
        sequence.initial();
        for (int i = 0; i < sequence.getEntriesCount(); i ++) {
            long currentId = sequence.getId();
            long currentCommitId = sequence.getCommitId();
            if (currentId != currentCommitId) {
                continue;
            }

            if (currentId == sequenceId) {
                return sequence.getCursor();
            }

            sequence.next();
        }

        return -1;
    }

    private long findMaxSequence(Sequence sequence, Data data) {
        long maxId = 0;
        long maxSequenceCursor = -1;

        sequence.initial();
        for (int i = 0; i < sequence.getEntriesCount(); i ++) {
            long currentId = sequence.getId();
            if (currentId <= 0) {
                continue;
            }

            long currentCommitId = sequence.getCommitId();
            if (currentId != currentCommitId) {
                continue;
            }

            if (currentId > maxId) {
                if (!checkElementConsistence(sequence, data, currentId)) {
                    continue;
                }

                maxId = currentId;
                maxSequenceCursor = sequence.getCursor();
            }

            sequence.next();
        }

        return maxSequenceCursor;
    }

    private long findMinSequenceBack(Sequence sequence, Data data) {
        long minId = sequence.getId();
        long minSequenceCursor = sequence.getCursor();

        for (int i = 0; i < sequence.getEntriesCount(); i ++) {
            long currentId = sequence.getId();
            if (currentId <= 0) {
                break;
            }

            long currentCommitId = sequence.getCommitId();
            if (currentId == currentCommitId) {
                break;
            }

            if (currentId < minId) {
                if (!checkElementConsistence(sequence, data, currentId)) {
                    break;
                }

                minId = currentId;
                minSequenceCursor = sequence.getCursor();
            }

            sequence.prev();
        }

        return minSequenceCursor;
    }

    private boolean checkElementConsistence(Sequence sequence, Data data, long currentId) {
        long elementSequenceId = data.getSequenceId(sequence.getElementCursor());
        long elementCommitSequenceId = data.getCommitSequenceId(sequence.getElementCursor(), sequence.getElementLength());
        return elementSequenceId == elementCommitSequenceId && elementSequenceId == currentId;
    }
}
