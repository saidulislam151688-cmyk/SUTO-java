package com.suto.exception;

/**
 * Exception thrown when user is not found
 * Demonstrates Inheritance - extends SutoException
 */
public class UserNotFoundException extends SutoException {

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

    public UserNotFoundException(String field, String value) {
        super("User not found with " + field + ": " + value);
    }
}
