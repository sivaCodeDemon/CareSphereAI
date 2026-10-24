package com.caresphere.common.exception;

/**
 * Thrown when authentication fails. Deliberately returns the same message for an
 * unknown email and a wrong password to avoid user enumeration. Mapped to HTTP 401.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
