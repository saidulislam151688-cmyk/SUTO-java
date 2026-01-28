package com.suto.service;

import com.suto.model.Graph;
import com.suto.model.Stop;
import com.suto.util.JsonFileService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GraphService {

    private final JsonFileService jsonFileService;
    private Graph graph;

    public GraphService() {
        this.jsonFileService = new JsonFileService();
        init();
    }

    private void init() {
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
    }
}
