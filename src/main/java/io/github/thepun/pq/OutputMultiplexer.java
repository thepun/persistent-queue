package io.github.thepun.pq;

final class OutputMultiplexer implements PersistentQueueHead<Object> {

    private final HeadCursor[] pipelines;

    OutputMultiplexer(HeadCursor[] pipelines) {
        this.pipelines = pipelines;
    }

    @Override
    public int get(Object[] batch, int offset, int length) {
        return 0;
    }

    // TODO: add park/unpark on wait
    @Override
    public int getOrWait(Object[] batch, int offset, int length) {
        int result;
        do {
            result = get(batch, offset, length);
        } while (result == 0);

        return result;
    }

    @Override
    public void commit() {

    }
}
