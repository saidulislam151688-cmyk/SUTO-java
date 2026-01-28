package com.suto.model;

import java.util.UUID;

public class Edge {
    private UUID id;
    private UUID stopId; // Target stop ID
    private String busName;
    private String mode; // "METRO" or "BUS"
    private double cost;
    private double distance;

    // Default constructor
    public Edge() {
    }

    // Constructor for Graph usage (target stopId, cost, distance, mode, busName)
    public Edge(UUID stopId, double cost, double distance, String mode, String busName) {
        this.stopId = stopId;
        this.cost = cost;
        this.distance = distance;
        this.mode = mode;
        this.busName = busName;
    }

    // All-args constructor
    public Edge(UUID id, UUID stopId, String busName, String mode) {
        this.id = id;
        this.stopId = stopId;
        this.busName = busName;
        this.mode = mode;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getStopId() {
        return stopId;
    }

    public String getBusName() {
        return busName;
    }

    public String getMode() {
        return mode;
    }

    public double getCost() {
        return cost;
    }

    public double getDistance() {
        return distance;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setStopId(UUID stopId) {
        this.stopId = stopId;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
