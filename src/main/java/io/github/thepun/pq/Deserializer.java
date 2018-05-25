package io.github.thepun.pq;

public interface Deserializer<T> {

    T deserialize(ReadBuffer buffer);

}
