package io.github.thepun.pq;

final class ScanResultElement {

    private int id;
    private long sequenceId;
    private long sequenceCursor;
    private long commitedSequenceId;
    private long minAvailableSequenceId;
    private long minAvailableSequenceCursor;
    private long maxAvailableSequenceId;
    private long maxAvailableSequenceCursor;
    private long minAvailableUncommittedSequenceId;
    private long minAvailableUncommittedSequenceCursor;
    private long maxAvailableUncommittedSequenceId;
    private long maxAvailableUncommittedSequenceCursor;
    private boolean uncommittedData;
    private boolean initial;

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

    boolean isUncommittedData() {
        return uncommittedData;
    }

    void setUncommittedData(boolean uncommittedData) {
        this.uncommittedData = uncommittedData;
    }

    boolean isInitial() {
        return initial;
    }

    void setInitial(boolean initial) {
        this.initial = initial;
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

    long getMinAvailableSequenceId() {
        return minAvailableSequenceId;
    }

    void setMinAvailableSequenceId(long minAvailableSequenceId) {
        this.minAvailableSequenceId = minAvailableSequenceId;
    }

    long getMinAvailableSequenceCursor() {
        return minAvailableSequenceCursor;
    }

    void setMinAvailableSequenceCursor(long minAvailableSequenceCursor) {
        this.minAvailableSequenceCursor = minAvailableSequenceCursor;
    }

    long getMaxAvailableSequenceId() {
        return maxAvailableSequenceId;
    }

    void setMaxAvailableSequenceId(long maxAvailableSequenceId) {
        this.maxAvailableSequenceId = maxAvailableSequenceId;
    }

    long getMaxAvailableSequenceCursor() {
        return maxAvailableSequenceCursor;
    }

    void setMaxAvailableSequenceCursor(long maxAvailableSequenceCursor) {
        this.maxAvailableSequenceCursor = maxAvailableSequenceCursor;
    }

    long getCommitedSequenceId() {
        return commitedSequenceId;
    }

    void setCommitedSequenceId(long commitedSequenceId) {
        this.commitedSequenceId = commitedSequenceId;
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
