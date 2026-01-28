package com.suto.service;

import com.suto.dto.RouteResponse;
import com.suto.exception.RouteNotFoundException;
import com.suto.model.Edge;
import com.suto.model.Graph;

import java.util.*;

public class PathFinderService {

    private final GraphService graphService;

    public PathFinderService(GraphService graphService) {
        this.graphService = graphService;
    }

    public RouteResponse findBestRoute(String originName, String destinationName) {
        Graph graph = graphService.getGraph();
        UUID startNode = graph.getStopId(originName);
        UUID endNode = graph.getStopId(destinationName);

        if (startNode == null || endNode == null) {
            throw new RouteNotFoundException(originName, destinationName);
        }

        RouteResponse response = new RouteResponse();
        response.setSource(originName);
        response.setDestination(destinationName);
        response.setStatus("success");

        // 1. Find Direct Routes (0 transfers)
        List<RouteResponse.DirectRoute> directRoutes = findDirectRoutes(graph, startNode, endNode);
        response.setDirectRoutes(directRoutes);

        // 2. Find Combined Routes (Transfers)
        List<RouteResponse.CombinedRoute> combinedRoutes = findCombinedRoutes(graph, startNode, endNode);

        if (!directRoutes.isEmpty()) {
            // Special Logic: If Direct routes exist, filter OUT bus-only combined routes
            // Keep ONLY combined routes that contain METRO
            combinedRoutes = combinedRoutes.stream()
                    .filter(this::hasMetro) // Keep only routes with metro
                    .collect(java.util.stream.Collectors.toList());
        }
        // ELSE: If no direct routes, show ALL combined routes (bus-only + metro)

        response.setCombinedRoutes(combinedRoutes);

        if (directRoutes.isEmpty() && combinedRoutes.isEmpty()) {
            response.setMessage("No routes found.");
        } else {
            response.setMessage("Routes found.");
        }

        return response;
    }

    // --- Direct Routes Logic (Enhanced) ---
    private List<RouteResponse.DirectRoute> findDirectRoutes(Graph graph, UUID start, UUID end) {
        List<RouteResponse.DirectRoute> routes = new ArrayList<>();

        // Use BFS to find all simple paths between start and end using a SINGLE
        // transport
        // Optimized: Check intersection of transport at start and end
        Set<String> startTransports = getTransportsAtNode(graph, start, true);
        Set<String> endTransports = getTransportsAtNode(graph, end, false);

        // Find common transports
        Set<String> commonTransports = new HashSet<>(startTransports);
        commonTransports.retainAll(endTransports);

        for (String transport : commonTransports) {
            // Verify connectivity for this specific transport
            List<UUID> path = findPathByTransport(graph, start, end, transport);
            if (!path.isEmpty()) {
                RouteResponse.DirectRoute route = new RouteResponse.DirectRoute();
                route.setName(transport);
                route.setType(isMetro(transport) ? "METRO" : "BUS");
                route.setStops(path.size() - 1);
                route.setDetails(transport + " (" + (path.size() - 1) + " stops)");
                routes.add(route);
            }
        }

        // Sort: Metro first, then fewer stops
        routes.sort(Comparator.comparing((RouteResponse.DirectRoute r) -> r.getType().equals("METRO") ? 0 : 1)
                .thenComparingInt(RouteResponse.DirectRoute::getStops)
                .thenComparing(RouteResponse.DirectRoute::getName)); // Stable sort

        return routes;
    }

    // --- Combined Routes Logic (Enhanced Multi-Path) ---
    private List<RouteResponse.CombinedRoute> findCombinedRoutes(Graph graph, UUID start, UUID end) {
        List<RouteResponse.CombinedRoute> allPlans = new ArrayList<>();

        // BFS to find multiple paths
        // State: {CurrentNode, PathSoFar, CurrentTransport, TransfersCount}
        Queue<State> queue = new LinkedList<>();
        queue.add(new State(start, null, 0, new ArrayList<>()));

        // Limit exploration to avoid explosion
        int limitChecked = 0;
        int maxChecked = 500000; // Enough to explore city graph deeply
        // Map to keep track of best cost to reach (Node, Transport) to prune bad paths
        Map<String, Integer> visitedState = new HashMap<>(); // Key: "NodeID_Transport", Value: Transfers

        List<State> validEndStates = new ArrayList<>();

        while (!queue.isEmpty() && limitChecked < maxChecked) {
            State current = queue.poll();
            limitChecked++;

            // Optimization: If transfers > 5, stop (allow complex city routes)
            if (current.transfers > 5)
                continue;

            // Pruning: If we reached this node+transport with FEWER transfers before, skip
            String stateKey = current.nodeId + "_" + (current.lastTransport == null ? "START" : current.lastTransport);
            if (visitedState.containsKey(stateKey) && visitedState.get(stateKey) < current.transfers) {
                continue;
            }
            // Allow re-visiting if same transfers but different transport (could be better
            // next leg)
            // But if strictly worse, skip.

            // Cycle detection in current path
            if (hasCycle(current.path))
                continue;

            if (current.nodeId.equals(end)) {
                validEndStates.add(current);
                continue; // Don't extend paths beyond destination
            }

            visitedState.put(stateKey, current.transfers);

            // Explore neighbors
            List<Edge> neighbors = graph.getNeighbors(current.nodeId);
            for (Edge edge : neighbors) {
                boolean isTransfer = current.lastTransport != null && !current.lastTransport.equals(edge.getBusName());
                int newTransfers = current.transfers + (isTransfer ? 1 : 0);

                // Construct new path
                List<Edge> newPath = new ArrayList<>(current.path);
                newPath.add(edge);

                // Add to queue
                // Prioritize "same transport" to encourage fewer transfers?
                // BFS naturally finds shortest by edges if unweighted.
                // We want filtered results later.
                queue.add(new State(edge.getStopId(), edge.getBusName(), newPath, newTransfers));
            }
        }

        // Convert valid end states to CombinedRoutes
        for (State s : validEndStates) {
            RouteResponse.CombinedRoute route = convertToCombinedRoute(graph, s.path, start);
            if (route != null) {
                allPlans.add(route);
            }
        }

        // Deduplicate: Keep best version (fewest stops) for each unique node sequence
        // (or segment sequence)
        // Python logic: "Identify unique paths by the sequence of start/end nodes of
        // segments"
        Map<String, RouteResponse.CombinedRoute> uniquePaths = new HashMap<>();

        for (RouteResponse.CombinedRoute r : allPlans) {
            String signature = getRouteSignature(r);
            if (!uniquePaths.containsKey(signature) || r.getTotalStops() < uniquePaths.get(signature).getTotalStops()) {
                uniquePaths.put(signature, r);
            }
        }

        List<RouteResponse.CombinedRoute> distinctRoutes = new ArrayList<>(uniquePaths.values());

        // Sort: Metro -> Transfers -> Stops
        distinctRoutes.sort((r1, r2) -> {
            boolean metro1 = hasMetro(r1);
            boolean metro2 = hasMetro(r2);
            if (metro1 != metro2)
                return metro1 ? -1 : 1;

            int transferDiff = Integer.compare(r1.getTotalSteps(), r2.getTotalSteps());
            if (transferDiff != 0)
                return transferDiff;

            return Integer.compare(r1.getTotalStops(), r2.getTotalStops());
        });

        // Return top 5
        return distinctRoutes.subList(0, Math.min(5, distinctRoutes.size()));
    }

    // --- New Metro-Centric Logic ---

    /**
     * Finds the optimal combined route that MUST use Metro for the middle leg.
     * Strategy: Find ALL reachable metro stations from source and destination,
     * evaluate ALL valid combinations, select the best one based on:
     * 1. Minimum transfers
     * 2. Minimum total stops
     */
    private RouteResponse.CombinedRoute findBestMetroRoute(Graph graph, UUID start, UUID end) {
        // Special case: If destination itself is a metro station, find direct metro
        // route
        if (isMetroStation(graph, end)) {
            // Try to find metro-only path from reachable metro station to destination
            List<MetroPathSegment> startMetroStations = findAllReachableMetroStations(graph, start);

            RouteResponse.CombinedRoute bestRoute = null;
            int bestTransfers = Integer.MAX_VALUE;
            int bestTotalStops = Integer.MAX_VALUE;

            for (MetroPathSegment startSegment : startMetroStations) {
                // Try direct metro path to destination
                List<Edge> metroPath = findMetroPath(graph, startSegment.metroNode, end);
                if (!metroPath.isEmpty()) {
                    List<Edge> fullPath = new ArrayList<>();
                    fullPath.addAll(startSegment.path);
                    fullPath.addAll(metroPath);

                    RouteResponse.CombinedRoute candidate = convertToCombinedRoute(graph, fullPath, start);
                    if (candidate != null) {
                        int transfers = candidate.getTotalSteps() - 1;
                        int totalStops = candidate.getTotalStops();

                        if (transfers < bestTransfers ||
                                (transfers == bestTransfers && totalStops < bestTotalStops)) {
                            bestRoute = candidate;
                            bestTransfers = transfers;
                            bestTotalStops = totalStops;
                        }
                    }
                }
            }

            if (bestRoute != null) {
                return bestRoute;
            }
        }

        // 1. Find ALL reachable metro stations from Start
        List<MetroPathSegment> startMetroStations = findAllReachableMetroStations(graph, start);

        // 2. Find ALL reachable metro stations from End
        List<MetroPathSegment> endMetroStations = findAllReachableMetroStations(graph, end);

        if (startMetroStations.isEmpty() || endMetroStations.isEmpty()) {
            return null; // No metro access from one or both endpoints
        }

        // 3. Evaluate ALL combinations and find the best route
        RouteResponse.CombinedRoute bestRoute = null;
        int bestTransfers = Integer.MAX_VALUE;
        int bestTotalStops = Integer.MAX_VALUE;

        for (MetroPathSegment startSegment : startMetroStations) {
            for (MetroPathSegment endSegment : endMetroStations) {
                UUID metroStartNode = startSegment.metroNode;
                UUID metroEndNode = endSegment.metroNode;

                // Skip if same station (no metro journey needed)
                if (metroStartNode.equals(metroEndNode)) {
                    continue;
                }

                // Find metro path between these two stations
                List<Edge> metroLegEdges = findMetroPath(graph, metroStartNode, metroEndNode);
                if (metroLegEdges.isEmpty()) {
                    continue; // These stations are not connected via metro
                }

                // Find path from metro end station to final destination
                List<Edge> lastLegEdges = findSimplePath(graph, metroEndNode, end);
                if (lastLegEdges == null) {
                    continue; // No path from metro station to destination
                }

                // Construct complete path
                List<Edge> fullPath = new ArrayList<>();
                fullPath.addAll(startSegment.path);
                fullPath.addAll(metroLegEdges);
                fullPath.addAll(lastLegEdges);

                // Convert to CombinedRoute and evaluate
                RouteResponse.CombinedRoute candidate = convertToCombinedRoute(graph, fullPath, start);
                if (candidate != null) {
                    int transfers = candidate.getTotalSteps() - 1; // Number of legs - 1 = transfers
                    int totalStops = candidate.getTotalStops();

                    // Select route with minimum transfers, then minimum stops
                    if (transfers < bestTransfers ||
                            (transfers == bestTransfers && totalStops < bestTotalStops)) {
                        bestRoute = candidate;
                        bestTransfers = transfers;
                        bestTotalStops = totalStops;
                    }
                }
            }
        }

        return bestRoute;
    }

    /**
     * Finds ALL reachable metro stations from a given starting node.
     * Returns a list of MetroPathSegment, each containing the metro station
     * and the path to reach it.
     */
    private List<MetroPathSegment> findAllReachableMetroStations(Graph graph, UUID startNode) {
        List<MetroPathSegment> metroStations = new ArrayList<>();

        // Special case: If start node is already a metro station, add it with empty
        // path
        if (isMetroStation(graph, startNode)) {
            metroStations.add(new MetroPathSegment(startNode, new ArrayList<>()));
        }

        Queue<PathState> q = new LinkedList<>();
        q.add(new PathState(startNode, new ArrayList<>()));
        Set<UUID> visited = new HashSet<>();
        visited.add(startNode);

        int limit = 50000; // Increased to ensure we find all reachable stations

        while (!q.isEmpty() && limit-- > 0) {
            PathState current = q.poll();

            // Check if current node is a metro station (but not the start node)
            if (!current.node.equals(startNode) && isMetroStation(graph, current.node)) {
                metroStations.add(new MetroPathSegment(current.node, current.path));
                // Continue exploring to find more metro stations
            }

            // Explore neighbors
            for (Edge e : graph.getNeighbors(current.node)) {
                if (!visited.contains(e.getStopId())) {
                    visited.add(e.getStopId());
                    List<Edge> newPath = new ArrayList<>(current.path);
                    newPath.add(e);
                    q.add(new PathState(e.getStopId(), newPath));
                }
            }
        }

        return metroStations;
    }

    private boolean isMetroStation(Graph graph, UUID nodeId) {
        for (Edge e : graph.getNeighbors(nodeId)) {
            if (isMetro(e.getBusName()))
                return true;
        }
        return false;
    }

    private List<Edge> findSimplePath(Graph graph, UUID start, UUID end) {
        Queue<PathState> q = new LinkedList<>();
        q.add(new PathState(start, new ArrayList<>()));
        Set<UUID> visited = new HashSet<>();
        visited.add(start);

        while (!q.isEmpty()) {
            PathState current = q.poll();
            if (current.node.equals(end))
                return current.path;

            for (Edge e : graph.getNeighbors(current.node)) {
                if (!visited.contains(e.getStopId())) {
                    visited.add(e.getStopId());
                    List<Edge> newPath = new ArrayList<>(current.path);
                    newPath.add(e);
                    q.add(new PathState(e.getStopId(), newPath));
                }
            }
        }
        return null; // Path not found
    }

    private List<Edge> findMetroPath(Graph graph, UUID start, UUID end) {
        // BFS strictly on Metro edges
        Queue<PathState> q = new LinkedList<>();
        q.add(new PathState(start, new ArrayList<>()));
        Set<UUID> visited = new HashSet<>();
        visited.add(start);

        while (!q.isEmpty()) {
            PathState current = q.poll();
            if (current.node.equals(end)) {
                return current.path;
            }

            for (Edge e : graph.getNeighbors(current.node)) {
                if (isMetro(e.getBusName()) && !visited.contains(e.getStopId())) {
                    visited.add(e.getStopId());
                    List<Edge> newPath = new ArrayList<>(current.path);
                    newPath.add(e);
                    q.add(new PathState(e.getStopId(), newPath));
                }
            }
        }
        return Collections.emptyList();
    }

    // Better structure classes
    private static class MetroPathSegment {
        UUID metroNode;
        List<Edge> path;

        public MetroPathSegment(UUID n, List<Edge> p) {
            metroNode = n;
            path = p;
        }
    }

    private static class PathState {
        UUID node;
        List<Edge> path;

        public PathState(UUID n, List<Edge> p) {
            node = n;
            path = p;
        }
    }

    private boolean hasCycle(List<Edge> path) {
        // Simple cycle check
        Set<UUID> visited = new HashSet<>();
        // we need start node too.. passed separately?
        // Actually, check edges target
        for (Edge e : path) {
            if (!visited.add(e.getStopId()))
                return true;
        }
        return false;
    }

    private String getRouteSignature(RouteResponse.CombinedRoute r) {
        StringBuilder sb = new StringBuilder();
        for (RouteResponse.RouteLeg leg : r.getLegs()) {
            sb.append(leg.getFrom()).append("->").append(leg.getTo()).append("|");
        }
        return sb.toString();
    }

    private boolean hasMetro(RouteResponse.CombinedRoute r) {
        for (RouteResponse.RouteLeg leg : r.getLegs()) {
            if ("METRO".equals(leg.getTransportMode()))
                return true;
        }
        return false;
    }

    private RouteResponse.CombinedRoute convertToCombinedRoute(Graph graph, List<Edge> pathEdges, UUID startNodeId) {
        if (pathEdges.isEmpty())
            return null;

        List<RouteResponse.RouteLeg> legs = new ArrayList<>();

        // Group edges into legs
        String currentTransport = pathEdges.get(0).getBusName();
        UUID legStart = startNodeId;
        int legStops = 0;

        // Track current node
        UUID currentNodeId = startNodeId;

        for (int i = 0; i < pathEdges.size(); i++) {
            Edge edge = pathEdges.get(i);

            if (!edge.getBusName().equals(currentTransport)) {
                // transport changed, close leg
                RouteResponse.RouteLeg leg = new RouteResponse.RouteLeg();
                leg.setFrom(graph.getStop(legStart).getName());
                leg.setTo(graph.getStop(currentNodeId).getName());
                leg.setTransportMode(isMetro(currentTransport) ? "METRO" : "BUS");
                leg.setOptions(Collections.singletonList(currentTransport)); // Simplify for now
                leg.setStopsCount(legStops);
                legs.add(leg);

                // Start new leg
                currentTransport = edge.getBusName();
                legStart = currentNodeId;
                legStops = 0;
            }

            legStops++;
            currentNodeId = edge.getStopId(); // Move to next node
        }

        // Add final leg
        RouteResponse.RouteLeg lastLeg = new RouteResponse.RouteLeg();
        lastLeg.setFrom(graph.getStop(legStart).getName());
        lastLeg.setTo(graph.getStop(currentNodeId).getName());
        lastLeg.setTransportMode(isMetro(currentTransport) ? "METRO" : "BUS");
        lastLeg.setOptions(Collections.singletonList(currentTransport));
        lastLeg.setStopsCount(legStops);
        legs.add(lastLeg);

        RouteResponse.CombinedRoute route = new RouteResponse.CombinedRoute();
        route.setLegs(legs);
        route.setTotalStops(pathEdges.size());
        route.setTotalSteps(legs.size());

        // Construct description
        StringBuilder desc = new StringBuilder();
        for (RouteResponse.RouteLeg leg : legs) {
            desc.append(leg.getFrom()).append(" -> ").append(leg.getTo())
                    .append(" (").append(leg.getTransportMode()).append(": ").append(leg.getOptions().get(0))
                    .append(")\n");
        }
        route.setDescription(desc.toString());

        return route;
    }

    private List<UUID> findPathByTransport(Graph graph, UUID start, UUID end, String transport) {
        // BFS for single transport
        Queue<List<UUID>> queue = new LinkedList<>();
        queue.add(Collections.singletonList(start));
        Set<UUID> visited = new HashSet<>();
        visited.add(start);

        while (!queue.isEmpty()) {
            List<UUID> path = queue.poll();
            UUID last = path.get(path.size() - 1);

            if (last.equals(end))
                return path;

            for (Edge edge : graph.getNeighbors(last)) {
                if (edge.getBusName().equals(transport) && !visited.contains(edge.getStopId())) {
                    visited.add(edge.getStopId());
                    List<UUID> newPath = new ArrayList<>(path);
                    newPath.add(edge.getStopId());
                    queue.add(newPath);
                }
            }
        }
        return Collections.emptyList();
    }

    private Set<String> getTransportsAtNode(Graph graph, UUID nodeId, boolean outgoing) {
        Set<String> transports = new HashSet<>();
        // In this undirected/bidirectional graph model, outgoing is sufficient
        for (Edge e : graph.getNeighbors(nodeId)) {
            transports.add(e.getBusName());
        }
        return transports;
    }

    private boolean isMetro(String name) {
        return name != null && (name.contains("MRT") || name.contains("Metro"));
    }

    private static class State {
        UUID nodeId;
        String lastTransport;
        int transfers;
        List<Edge> path;

        public State(UUID nodeId, String lastTransport, int cost, List<Edge> path) {
            this(nodeId, lastTransport, path, 0); // Cost ignored
        }

        public State(UUID nodeId, String lastTransport, List<Edge> path, int transfers) {
            this.nodeId = nodeId;
            this.lastTransport = lastTransport;
            this.path = path;
            this.transfers = transfers;
        }
    }
}
