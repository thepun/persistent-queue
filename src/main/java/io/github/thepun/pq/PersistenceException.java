package io.github.thepun.pq;

public final class PersistenceException extends Exception {

    PersistenceException(String message) {
        super(message);
    }

    PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
