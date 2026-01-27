package com.suto.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class BookingRequest {
    private String busId; // Assuming passed as String from JSON
    private String fromStopId;
    private String toStopId;
    private Double fare;
}
