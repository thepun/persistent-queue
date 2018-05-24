package io.github.thepun.pq;

public interface Callback<T, C> {

    void onElementPersisted(T element, C elementContext);

}
