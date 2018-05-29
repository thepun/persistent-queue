package io.github.thepun.pq;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class PersistentQueue<T, C> {

    private final PersistentQueueHead<T>[] heads;
    private final PersistentQueueTail<T, C>[] tails;

    private Thread persisterThread;

    public PersistentQueue(Configuration<T, C> configuration) {
        PersistentQueueHead<T>[] headsToUse = new PersistentQueueHead[configuration.getHeadCount()];
        for (int i = 0; i < headsToUse.length; i++) {
            headsToUse[i] = (PersistentQueueHead<T>) new QueueFromPersister((Configuration<Object, Object>) configuration);
        }
        heads = headsToUse;

        PersistentQueueTail<T, C>[] tailsToUse = new PersistentQueueTail[configuration.getTailCount()];
        for (int i = 0; i < tailsToUse.length; i++) {
            tailsToUse[i] = (PersistentQueueTail<T, C>) new QueueToPersister((Configuration<Object, Object>) configuration);
        }
        tails = tailsToUse;
    }

    public PersistentQueueHead<T> getHead(int index) {
        if (index < 0 || index > heads.length) {
            throw new IllegalArgumentException("Wrong head index");
        }

        return heads[index];
    }

    public PersistentQueueTail<T, C> getTail(int index) {
        if (index < 0 || index > tails.length) {
            throw new IllegalArgumentException("Wrong tail index");
        }

        return tails[index];
    }

    public synchronized void start() {

    }

    public synchronized void stop() {

    }

    private static Serializer<Object, Object>[] initSerializers(Map<Class<?>, Serializer<?, ?>> serializersMap) {
        int size = nextGreaterPrime(serializersMap.size());
        Serializer<Object, Object>[] array = new Serializer[size];

        upperLoop:
        for (; ; ) {
            for (Map.Entry<Class<?>, Serializer<?, ?>> entry : serializersMap.entrySet()) {
                Class<?> type = entry.getKey();
                int typeHash = type.hashCode() % size;
                Serializer<?, ?> anotherSerializer = array[typeHash];
                if (anotherSerializer != null) {
                    size = nextGreaterPrime(size);
                    array = new Serializer[size];
                    continue upperLoop;
                }

                array[typeHash] = (Serializer<Object, Object>) entry.getValue();
            }

            break;
        }

        return array;
    }

    private static int nextGreaterPrime(int value) {
        do {
            value++;
        } while (!isPrime(value));

        return value;
    }

    private static boolean isPrime(int n) {
        if (n % 2 == 0) {
            return false;
        }

        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }

        return true;
    }
}
