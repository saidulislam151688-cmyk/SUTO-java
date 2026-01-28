package com.suto.model;

import java.util.UUID;

/**
 * Stop Entity
 * Demonstrates:
 * 1. Inheritance (extends BaseEntity)
 * 2. Polymorphism
 */
public class Stop extends BaseEntity {

    private String name;
    private String type; // "METRO", "BUS"
    private Double lat; // Latitude
    private Double lng; // Longitude

    public Stop() {
        super();
    }

    public Stop(UUID id, String name, String type) {
        super(id, null);
        this.name = name;
        this.type = type;
    }

    // Getters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    /**
     * Polymorphism Example
     */
    @Override
    public String getEntityName() {
        return "Stop";
    }
}
