package com.suto.model;

import java.util.*;

public class Graph {
    // Map of Stop UUID -> List of Edges (neighbors)
    private final Map<UUID, List<Edge>> adjacencyList = new HashMap<>();

    // Map of Stop Name -> Stop UUID (for searching)
    private final Map<String, UUID> nameToId = new HashMap<>();

    // Map of Stop UUID -> Stop Object (for details)
    private final Map<UUID, Stop> stops = new HashMap<>();

    public void addStop(Stop stop) {
        stops.put(stop.getId(), stop);
        nameToId.put(stop.getName().toLowerCase(), stop.getId());
        adjacencyList.putIfAbsent(stop.getId(), new ArrayList<>());
    }

    public void addEdge(UUID from, UUID to, double cost, double distance, String mode, String busName) {
        adjacencyList.get(from).add(new Edge(to, cost, distance, mode, busName));
    }

    public List<Edge> getNeighbors(UUID stopId) {
        return adjacencyList.getOrDefault(stopId, new ArrayList<>());
    }

    // Helper to get edges between specific nodes (for multi-graph behavior)
    public List<Edge> getEdges(UUID from, UUID to) {
        List<Edge> edges = new ArrayList<>();
        List<Edge> neighbors = getNeighbors(from);
        for (Edge e : neighbors) {
            if (e.getStopId().equals(to)) {
                edges.add(e);
            }
        }
        return edges;
    }

    public UUID getStopId(String name) {
        return nameToId.get(name.toLowerCase());
    }

    public Stop getStop(UUID id) {
        return stops.get(id);
    }

    public Collection<Stop> getStops() {
        return stops.values();
    }
}
