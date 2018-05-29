package io.github.thepun.pq;

public interface PersistCallback<T, C> {

    void onElementPersisted(T element, C elementContext);

}
