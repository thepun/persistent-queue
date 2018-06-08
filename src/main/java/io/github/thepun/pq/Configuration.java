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
