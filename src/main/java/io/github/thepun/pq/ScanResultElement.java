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

final class ScanResultElement {

    private int id;
    private long dataCursor;
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
    private Sequence sequence;

    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public long getDataCursor() {
        return dataCursor;
    }

    public void setDataCursor(long dataCursor) {
        this.dataCursor = dataCursor;
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
