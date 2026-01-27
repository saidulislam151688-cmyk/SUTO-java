package com.suto.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bus {
    private UUID id;
    private String nameEn;
    private String nameBn;
    private String imageUrl;
    private String serviceType; // bus, metro
    private LocalDateTime createdAt;
}
