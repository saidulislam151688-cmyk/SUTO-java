package com.suto.exception;

/**
 * Exception thrown when login credentials are invalid
 * Demonstrates Inheritance - extends SutoException
 */
public class InvalidCredentialsException extends SutoException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
