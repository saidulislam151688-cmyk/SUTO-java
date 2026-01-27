package com.suto.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String role; // passenger, driver, owner
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
