package io.github.thepun.pq;

final class ScanCommitElement {

    private long id;
    private long sequenceId;
    private long sequenceCursor;
    private long firstUncommittedSequenceId;
    private long firstUncommittedSequenceCursor;
    private long minAvailableUncommittedSequenceId;
    private long minAvailableUncommittedSequenceCursor;
    private long maxAvailableUncommittedSequenceId;
    private long maxAvailableUncommittedSequenceCursor;
    private boolean hasUncommittedData;
    private Commit commit;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public long getSequenceCursor() {
        return sequenceCursor;
    }

    public void setSequenceCursor(long sequenceCursor) {
        this.sequenceCursor = sequenceCursor;
    }

    public long getFirstUncommittedSequenceId() {
        return firstUncommittedSequenceId;
    }

    public void setFirstUncommittedSequenceId(long firstUncommittedSequenceId) {
        this.firstUncommittedSequenceId = firstUncommittedSequenceId;
    }

    public long getFirstUncommittedSequenceCursor() {
        return firstUncommittedSequenceCursor;
    }

    public void setFirstUncommittedSequenceCursor(long firstUncommittedSequenceCursor) {
        this.firstUncommittedSequenceCursor = firstUncommittedSequenceCursor;
    }

    public boolean isHasUncommittedData() {
        return hasUncommittedData;
    }

    public void setHasUncommittedData(boolean hasUncommittedData) {
        this.hasUncommittedData = hasUncommittedData;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    public long getMinAvailableUncommittedSequenceId() {
        return minAvailableUncommittedSequenceId;
    }

    public void setMinAvailableUncommittedSequenceId(long minAvailableUncommittedSequenceId) {
        this.minAvailableUncommittedSequenceId = minAvailableUncommittedSequenceId;
    }

    public long getMinAvailableUncommittedSequenceCursor() {
        return minAvailableUncommittedSequenceCursor;
    }

    public void setMinAvailableUncommittedSequenceCursor(long minAvailableUncommittedSequenceCursor) {
        this.minAvailableUncommittedSequenceCursor = minAvailableUncommittedSequenceCursor;
    }

    public long getMaxAvailableUncommittedSequenceId() {
        return maxAvailableUncommittedSequenceId;
    }

    public void setMaxAvailableUncommittedSequenceId(long maxAvailableUncommittedSequenceId) {
        this.maxAvailableUncommittedSequenceId = maxAvailableUncommittedSequenceId;
    }

    public long getMaxAvailableUncommittedSequenceCursor() {
        return maxAvailableUncommittedSequenceCursor;
    }

    public void setMaxAvailableUncommittedSequenceCursor(long maxAvailableUncommittedSequenceCursor) {
        this.maxAvailableUncommittedSequenceCursor = maxAvailableUncommittedSequenceCursor;
    }
}
