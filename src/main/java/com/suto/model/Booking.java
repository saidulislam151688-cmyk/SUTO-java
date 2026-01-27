package com.suto.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private UUID id;
    private UUID userId;
    private UUID busId;
    private UUID fromStopId;
    private UUID toStopId;
    private LocalDateTime bookingTime;
    private String status; // pending, confirmed, cancelled
    private Double fare;
    private LocalDateTime createdAt;

    // Legacy / Driver Assignment Fields
    private UUID driverId;
    private UUID ownerId;
    private String origin;
    private String destination;
    private java.time.LocalDate tripDate;
    private java.time.LocalTime tripTime;
}
