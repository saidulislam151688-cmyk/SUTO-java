package com.suto.exception;

/**
 * Exception thrown when duplicate email is detected
 * Demonstrates Inheritance - extends SutoException
 */
public class DuplicateEmailException extends SutoException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}
