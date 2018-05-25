package io.github.thepun.pq;

public final class PersistenceException extends RuntimeException {

    PersistenceException(String message) {
        super(message);
    }

    PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
