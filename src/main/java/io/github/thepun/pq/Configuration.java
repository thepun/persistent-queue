package io.github.thepun.pq;

import java.util.Map;
import java.util.concurrent.ThreadFactory;

public final class Configuration<T, C> {

    private int headCount;
    private int tailCount;
    private int dataFileSize;
    private int sequenceFileSize;
    private String dataPath;
    private ThreadFactory persisterThreadFactory;
    private PersistCallback<T, C> persistCallback;
    private Map<Class<? extends T>, Marshaller<? extends T, ? extends C>> serializers;

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

    public Map<Class<? extends T>, Marshaller<? extends T, ? extends C>> getSerializers() {
        return serializers;
    }

    public void setSerializers(Map<Class<? extends T>, Marshaller<? extends T, ? extends C>> serializers) {
        this.serializers = serializers;
    }

    public PersistCallback<T, C> getPersistCallback() {
        return persistCallback;
    }

    public void setPersistCallback(PersistCallback<T, C> persistCallback) {
        this.persistCallback = persistCallback;
    }

    public int getDataFileSize() {
        return dataFileSize;
    }

    public void setDataFileSize(int dataFileSize) {
        this.dataFileSize = dataFileSize;
    }

    public int getSequenceFileSize() {
        return sequenceFileSize;
    }

    public void setSequenceFileSize(int sequenceFileSize) {
        this.sequenceFileSize = sequenceFileSize;
    }

    public ThreadFactory getPersisterThreadFactory() {
        return persisterThreadFactory;
    }

    public void setPersisterThreadFactory(ThreadFactory persisterThreadFactory) {
        this.persisterThreadFactory = persisterThreadFactory;
    }
}
