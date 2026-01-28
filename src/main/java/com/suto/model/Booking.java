package com.suto.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Booking Entity
 * Demonstrates:
 * 1. Inheritance (extends BaseEntity)
 * 2. Encapsulation (private fields)
 * 3. Polymorphism (overrides getEntityName)
 */
public class Booking extends BaseEntity {

    // Booking specific fields
    private UUID userId;
    private UUID routeId;
    private String busName;
    private LocalDateTime travelDate;
    private LocalDateTime bookingTime;
    private String status; // "CONFIRMED", "CANCELLED"
    private String seatNumber; // e.g., "A1"
    private Double fare;

    // Legacy fields (kept for compatibility)
    private UUID busId;
    private UUID driverId;
    private UUID fromStopId;
    private UUID toStopId;
    private int numberOfSeats;
    private double totalPrice;

    // Default constructor
    public Booking() {
        super();
    }

    // Parameterized constructor
    public Booking(UUID id, UUID userId, UUID routeId, String busName, LocalDateTime travelDate, String status) {
        super(id, LocalDateTime.now());
        this.userId = userId;
        this.routeId = routeId;
        this.busName = busName;
        this.travelDate = travelDate;
        this.status = status;
        this.bookingTime = LocalDateTime.now();
    }

    // Getters & Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getRouteId() {
        return routeId;
    }

    public void setRouteId(UUID routeId) {
        this.routeId = routeId;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public LocalDateTime getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDateTime travelDate) {
        this.travelDate = travelDate;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Double getFare() {
        return fare;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }

    // Legacy Getters/Setters
    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public UUID getFromStopId() {
        return fromStopId;
    }

    public void setFromStopId(UUID fromStopId) {
        this.fromStopId = fromStopId;
    }

    public UUID getToStopId() {
        return toStopId;
    }

    public void setToStopId(UUID toStopId) {
        this.toStopId = toStopId;
    }

    public UUID getBusId() {
        return busId;
    }

    public void setBusId(UUID busId) {
        this.busId = busId;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }

    /**
     * Polymorphism Example
     */
    @Override
    public String getEntityName() {
        return "Booking";
    }
}
