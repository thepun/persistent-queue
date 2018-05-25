package io.github.thepun.pq;

public interface Serializer<T, C> {

    void serialize(WriteBuffer buffer, T object, C objectContext);

}
