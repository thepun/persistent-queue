package io.github.thepun.pq;

public interface Marshaller<T, C> {

    int getTypeId();

    void serialize(WriteBuffer buffer, T object, C objectContext);

    T deserialize(ReadBuffer buffer);

}
