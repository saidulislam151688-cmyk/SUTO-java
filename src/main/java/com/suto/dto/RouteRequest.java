package com.suto.dto;

public class RouteRequest {
    private String source;
    private String destination;

    // Default constructor
    public RouteRequest() {
    }

    // All-args constructor
    public RouteRequest(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    // Getters
    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getOrigin() {
        return source; // Alias for backward compatibility
    }

    // Setters
    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
