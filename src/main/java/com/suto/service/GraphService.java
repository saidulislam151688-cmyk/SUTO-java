package com.suto.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suto.model.Graph;
import com.suto.model.Stop;
import com.suto.model.Edge;
import com.suto.util.JsonFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GraphService {

    @Autowired
    private JsonFileService jsonFileService;

    private Graph graph;

    @PostConstruct
    public void init() {
        refreshGraph();
    }

    public Graph getGraph() {
        return graph;
    }

    public void refreshGraph() {
        Graph newGraph = new Graph();

        try {
            // Load transport graph from JSON file
            Map<String, Object> transportData = jsonFileService.readObject("transport_graph.json", Map.class);

            if (transportData == null || !transportData.containsKey("nodes")) {
                System.err.println("No transport data found, initializing empty graph");
                this.graph = newGraph;
                return;
            }

            // Load nodes (stops)
            @SuppressWarnings("unchecked")
            List<Map<String, String>> nodes = (List<Map<String, String>>) transportData.get("nodes");

            for (Map<String, String> node : nodes) {
                Stop stop = new Stop();
                stop.setId(UUID.randomUUID());
                stop.setName(node.get("id")); // The "id" field is actually the stop name
                // You can add lat/lng if available in the data
                stop.setLat(0.0); // Default for now
                stop.setLng(0.0); // Default for now
                newGraph.addStop(stop);
            }

            // Load edges (connections)
            if (transportData.containsKey("links")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> edges = (List<Map<String, Object>>) transportData.get("links");

                for (Map<String, Object> edge : edges) {
                    String fromName = (String) edge.get("source");
                    String toName = (String) edge.get("target");
                    String mode = (String) edge.get("mode");
                    String busName = (String) edge.getOrDefault("transport", "Unknown");

                    Number cost = (Number) edge.getOrDefault("cost", 10.0);
                    Number distance = (Number) edge.getOrDefault("distance", 1.0);

                    UUID fromId = newGraph.getStopId(fromName);
                    UUID toId = newGraph.getStopId(toName);

                    if (fromId != null && toId != null) {
                        newGraph.addEdge(fromId, toId, cost.doubleValue(), distance.doubleValue(), mode, busName);
                    }
                }
            }

            this.graph = newGraph;
            System.out
                    .println("✅ Graph loaded with " + newGraph.getStops().size() + " stops from transport_graph.json");

        } catch (Exception e) {
            System.err.println("⚠️ Error loading transport graph: " + e.getMessage());
            e.printStackTrace();
            this.graph = newGraph; // Use empty graph if loading fails
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
