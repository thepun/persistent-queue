package io.github.thepun.pq;

final class ScanResultElement {

    private int id;
    private long sequenceId;
    private long sequenceCursor;
    private long firstUncommittedSequenceId;
    private long firstUncommittedSequenceCursor;
    private long minAvailableUncommittedSequenceId;
    private long minAvailableUncommittedSequenceCursor;
    private long maxAvailableUncommittedSequenceId;
    private long maxAvailableUncommittedSequenceCursor;
    private boolean uncommittedData;

    private Data data;
    private Commit commit;
    private Sequence sequence;

    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    long getSequenceId() {
        return sequenceId;
    }

    void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    long getSequenceCursor() {
        return sequenceCursor;
    }

    void setSequenceCursor(long sequenceCursor) {
        this.sequenceCursor = sequenceCursor;
    }

    long getFirstUncommittedSequenceId() {
        return firstUncommittedSequenceId;
    }

    void setFirstUncommittedSequenceId(long firstUncommittedSequenceId) {
        this.firstUncommittedSequenceId = firstUncommittedSequenceId;
    }

    long getFirstUncommittedSequenceCursor() {
        return firstUncommittedSequenceCursor;
    }

    void setFirstUncommittedSequenceCursor(long firstUncommittedSequenceCursor) {
        this.firstUncommittedSequenceCursor = firstUncommittedSequenceCursor;
    }

    boolean isUncommittedData() {
        return uncommittedData;
    }

    void setUncommittedData(boolean uncommittedData) {
        this.uncommittedData = uncommittedData;
    }

    Commit getCommit() {
        return commit;
    }

    void setCommit(Commit commit) {
        this.commit = commit;
    }

    long getMinAvailableUncommittedSequenceId() {
        return minAvailableUncommittedSequenceId;
    }

    void setMinAvailableUncommittedSequenceId(long minAvailableUncommittedSequenceId) {
        this.minAvailableUncommittedSequenceId = minAvailableUncommittedSequenceId;
    }

    long getMinAvailableUncommittedSequenceCursor() {
        return minAvailableUncommittedSequenceCursor;
    }

    void setMinAvailableUncommittedSequenceCursor(long minAvailableUncommittedSequenceCursor) {
        this.minAvailableUncommittedSequenceCursor = minAvailableUncommittedSequenceCursor;
    }

    long getMaxAvailableUncommittedSequenceId() {
        return maxAvailableUncommittedSequenceId;
    }

    void setMaxAvailableUncommittedSequenceId(long maxAvailableUncommittedSequenceId) {
        this.maxAvailableUncommittedSequenceId = maxAvailableUncommittedSequenceId;
    }

    long getMaxAvailableUncommittedSequenceCursor() {
        return maxAvailableUncommittedSequenceCursor;
    }

    void setMaxAvailableUncommittedSequenceCursor(long maxAvailableUncommittedSequenceCursor) {
        this.maxAvailableUncommittedSequenceCursor = maxAvailableUncommittedSequenceCursor;
    }

    Data getData() {
        return data;
    }

    void setData(Data data) {
        this.data = data;
    }

    Sequence getSequence() {
        return sequence;
    }

    void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }
}
