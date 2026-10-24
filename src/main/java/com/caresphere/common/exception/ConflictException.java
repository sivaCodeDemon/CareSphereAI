package com.caresphere.common.exception;

/**
 * Thrown when a request would create a duplicate of an existing resource. Mapped to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
