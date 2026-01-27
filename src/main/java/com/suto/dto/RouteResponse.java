package com.suto.dto;

import lombok.Data;
import java.util.List;

@Data
public class RouteResponse {
    private String source;
    private String destination;
    private List<DirectRoute> directRoutes;
    private List<CombinedRoute> combinedRoutes;
    private String status;
    private String message;

    // Legacy fields for backward compatibility
    private List<RouteSegment> segments;
    private double totalCost;
    private double totalDistance;
    private double estimatedTime;

    @Data
    public static class DirectRoute {
        private String type; // "METRO" or "BUS"
        private String name; // Transport name
        private int stops;
        private String details;
    }

    @Data
    public static class CombinedRoute {
        private int totalStops;
        private int totalSteps;
        private List<RouteLeg> legs;
        private String description;
    }

    @Data
    public static class RouteLeg {
        private String from;
        private String to;
        private String transportMode; // "METRO" or "BUS"
        private List<String> options; // List of transport names
        private int stopsCount;
    }

    @Data
    public static class RouteSegment {
        private String fromStop;
        private String toStop;
        private String mode;
        private String busName;
        private double cost;
    }
}
