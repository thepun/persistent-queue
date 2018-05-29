package io.github.thepun.pq;

import java.util.Map;

public final class Configuration<T, C> {

    private boolean sync;
    private int headCount;
    private int tailCount;
    private String dataPath;
    private int inputBatchSize;
    private int outputBatchSize;
    private int initialFreeNodes;
    private PersistCallback<T, C> persistCallback;
    private Map<Class<? extends T>, Serializer<? extends T, ? extends C>> serializers;

    public int getTailCount() {
        return tailCount;
    }

    public void setTailCount(int tailCount) {
        this.tailCount = tailCount;
    }

    public int getHeadCount() {
        return headCount;
    }

    public void setHeadCount(int headCount) {
        this.headCount = headCount;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public Map<Class<? extends T>, Serializer<? extends T, ? extends C>> getSerializers() {
        return serializers;
    }

    public void setSerializers(Map<Class<? extends T>, Serializer<? extends T, ? extends C>> serializers) {
        this.serializers = serializers;
    }

    public PersistCallback<T, C> getPersistCallback() {
        return persistCallback;
    }

    public void setPersistCallback(PersistCallback<T, C> persistCallback) {
        this.persistCallback = persistCallback;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public int getInitialFreeNodes() {
        return initialFreeNodes;
    }

    public void setInitialFreeNodes(int initialFreeNodes) {
        this.initialFreeNodes = initialFreeNodes;
    }

    public int getInputBatchSize() {
        return inputBatchSize;
    }

    public void setInputBatchSize(int inputBatchSize) {
        this.inputBatchSize = inputBatchSize;
    }

    public int getOutputBatchSize() {
        return outputBatchSize;
    }

    public void setOutputBatchSize(int outputBatchSize) {
        this.outputBatchSize = outputBatchSize;
    }
}
