package com.suto.model;

import java.util.List;
import java.util.UUID;

/**
 * Bus Entity
 * Demonstrates:
 * 1. Inheritance (extends BaseEntity)
 * 2. Encapsulation
 */
public class Bus extends BaseEntity {

    private String name; // Bus name (e.g., "Green Line")
    private String route; // Route name (e.g., "Airport-Mirpur")
    private String type; // "AC", "NON_AC"
    private List<UUID> stops; // List of stop IDs

    // Additional fields
    private String busNumber;
    private Integer capacity;

    public Bus() {
        super();
    }

    public Bus(UUID id, String name, String route, String type, List<UUID> stops) {
        super(id, null); // CreatedAt is handled by BaseEntity or Service
        this.name = name;
        this.route = route;
        this.type = type;
        this.stops = stops;
    }

    // Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Alias for compatibility
    public String getBusName() {
        return name;
    }

    public void setBusName(String name) {
        this.name = name;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<UUID> getStops() {
        return stops;
    }

    public void setStops(List<UUID> stops) {
        this.stops = stops;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    @Override
    public String getEntityName() {
        return "Bus";
    }
}
