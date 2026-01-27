package com.suto.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {
    private UUID id;
    private String name;
    private Double lat;
    private Double lng;
    private LocalDateTime createdAt;
}
