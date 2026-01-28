package com.suto.dto;

import java.util.List;

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

    // Default constructor
    public RouteResponse() {
    }

    // Getters and Setters
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<DirectRoute> getDirectRoutes() {
        return directRoutes;
    }

    public void setDirectRoutes(List<DirectRoute> directRoutes) {
        this.directRoutes = directRoutes;
    }

    public List<CombinedRoute> getCombinedRoutes() {
        return combinedRoutes;
    }

    public void setCombinedRoutes(List<CombinedRoute> combinedRoutes) {
        this.combinedRoutes = combinedRoutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<RouteSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<RouteSegment> segments) {
        this.segments = segments;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(double estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    // Nested class: DirectRoute
    public static class DirectRoute {
        private String type; // "METRO" or "BUS"
        private String name; // Transport name
        private int stops;
        private String details;

        public DirectRoute() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStops() {
            return stops;
        }

        public void setStops(int stops) {
            this.stops = stops;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }

    // Nested class: CombinedRoute
    public static class CombinedRoute {
        private int totalStops;
        private int totalSteps;
        private List<RouteLeg> legs;
        private String description;

        public CombinedRoute() {
        }

        public int getTotalStops() {
            return totalStops;
        }

        public void setTotalStops(int totalStops) {
            this.totalStops = totalStops;
        }

        public int getTotalSteps() {
            return totalSteps;
        }

        public void setTotalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
        }

        public List<RouteLeg> getLegs() {
            return legs;
        }

        public void setLegs(List<RouteLeg> legs) {
            this.legs = legs;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // Nested class: RouteLeg
    public static class RouteLeg {
        private String from;
        private String to;
        private String transportMode; // "METRO" or "BUS"
        private List<String> options; // List of transport names
        private int stopsCount;

        public RouteLeg() {
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getTransportMode() {
            return transportMode;
        }

        public void setTransportMode(String transportMode) {
            this.transportMode = transportMode;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public int getStopsCount() {
            return stopsCount;
        }

        public void setStopsCount(int stopsCount) {
            this.stopsCount = stopsCount;
        }
    }

    // Nested class: RouteSegment
    public static class RouteSegment {
        private String fromStop;
        private String toStop;
        private String mode;
        private String busName;
        private double cost;

        public RouteSegment() {
        }

        public String getFromStop() {
            return fromStop;
        }

        public void setFromStop(String fromStop) {
            this.fromStop = fromStop;
        }

        public String getToStop() {
            return toStop;
        }

        public void setToStop(String toStop) {
            this.toStop = toStop;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getBusName() {
            return busName;
        }

        public void setBusName(String busName) {
            this.busName = busName;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }
    }
}
