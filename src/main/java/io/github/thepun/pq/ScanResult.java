package io.github.thepun.pq;

final class ScanResult {

    private Data data;
    private Sequence sequence;
    private Commit[] commits;
    private ScanCommitElement[] scannedCommits;

    private long maxAvailableSequnceId;
    private long maxAvailableSequnceCursor;
    private long minAvailableSequenceId;
    private long minAvailableSequenceCursor;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public Commit[] getCommits() {
        return commits;
    }

    public void setCommits(Commit[] commits) {
        this.commits = commits;
    }

    public ScanCommitElement[] getScannedCommits() {
        return scannedCommits;
    }

    public void setScannedCommits(ScanCommitElement[] scannedCommits) {
        this.scannedCommits = scannedCommits;
    }

    public long getMinAvailableSequenceId() {
        return minAvailableSequenceId;
    }

    public void setMinAvailableSequenceId(long minAvailableSequenceId) {
        this.minAvailableSequenceId = minAvailableSequenceId;
    }

    public long getMinAvailableSequenceCursor() {
        return minAvailableSequenceCursor;
    }

    public void setMinAvailableSequenceCursor(long minAvailableSequenceCursor) {
        this.minAvailableSequenceCursor = minAvailableSequenceCursor;
    }

    public long getMaxAvailableSequnceId() {
        return maxAvailableSequnceId;
    }

    public void setMaxAvailableSequnceId(long maxAvailableSequnceId) {
        this.maxAvailableSequnceId = maxAvailableSequnceId;
    }

    public long getMaxAvailableSequnceCursor() {
        return maxAvailableSequnceCursor;
    }

    public void setMaxAvailableSequnceCursor(long maxAvailableSequnceCursor) {
        this.maxAvailableSequnceCursor = maxAvailableSequnceCursor;
    }
}
