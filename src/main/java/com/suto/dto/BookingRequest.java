package com.suto.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookingRequest {
    private UUID routeId;
    private String busId; // String to convert to UUID
    private String fromStopId; // String to convert to UUID
    private String toStopId; // String to convert to UUID
    private String busName;
    private LocalDateTime travelDate;
    private int numberOfSeats;
    private double fare;

    // Default constructor
    public BookingRequest() {
    }

    // All-args constructor
    public BookingRequest(UUID routeId, String busName, LocalDateTime travelDate, int numberOfSeats) {
        this.routeId = routeId;
        this.busName = busName;
        this.travelDate = travelDate;
        this.numberOfSeats = numberOfSeats;
    }

    // Getters
    public UUID getRouteId() {
        return routeId;
    }

    public String getBusId() {
        return busId;
    }

    public String getFromStopId() {
        return fromStopId;
    }

    public String getToStopId() {
        return toStopId;
    }

    public String getBusName() {
        return busName;
    }

    public LocalDateTime getTravelDate() {
        return travelDate;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public double getFare() {
        return fare;
    }

    // Setters
    public void setRouteId(UUID routeId) {
        this.routeId = routeId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public void setFromStopId(String fromStopId) {
        this.fromStopId = fromStopId;
    }

    public void setToStopId(String toStopId) {
        this.toStopId = toStopId;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public void setTravelDate(LocalDateTime travelDate) {
        this.travelDate = travelDate;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }
}
