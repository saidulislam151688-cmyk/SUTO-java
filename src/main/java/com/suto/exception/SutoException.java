package com.suto.exception;

/**
 * Base Exception for SUTO Application
 * Demonstrates Exception Hierarchy & Inheritance
 * All custom exceptions extend this class
 */
public class SutoException extends RuntimeException {

    // Default constructor
    public SutoException() {
        super();
    }

    // Constructor with message
    public SutoException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public SutoException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor with cause
    public SutoException(Throwable cause) {
        super(cause);
    }
}
