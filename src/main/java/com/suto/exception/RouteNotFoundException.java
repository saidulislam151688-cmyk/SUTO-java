package com.suto.exception;

/**
 * Exception thrown when route is not found
 * Demonstrates Inheritance - extends SutoException
 */
public class RouteNotFoundException extends SutoException {

    public RouteNotFoundException(String source, String destination) {
        super("No route found from " + source + " to " + destination);
    }

    public RouteNotFoundException(String message) {
        super(message);
    }
}
