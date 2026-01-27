package com.suto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Edge {
    private UUID stopId;
    private double cost;
    private double distance;
    private String mode; // BUS or METRO
    private String busName;
}
