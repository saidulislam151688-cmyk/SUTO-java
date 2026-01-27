package com.suto.service;

import com.suto.dto.RouteResponse;
import com.suto.model.Edge;
import com.suto.model.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PathFinderService {

    @Autowired
    private GraphService graphService;
    // ... (rest of the file remains same, just cleaning imports)

    public RouteResponse findBestRoute(String originName, String destinationName) {
        Graph graph = graphService.getGraph();
        UUID startNode = graph.getStopId(originName);
        UUID endNode = graph.getStopId(destinationName);

        if (startNode == null || endNode == null) {
            throw new RuntimeException("One or both locations not found: " +
                    (startNode == null ? originName : "") + " " +
                    (endNode == null ? destinationName : ""));
        }

        RouteResponse response = new RouteResponse();
        response.setSource(originName);
        response.setDestination(destinationName);
        response.setStatus("success");

        // 1. Find Direct Routes (0 transfers)
        List<RouteResponse.DirectRoute> directRoutes = findDirectRoutes(graph, startNode, endNode);
        response.setDirectRoutes(directRoutes);

        // 2. Find Combined Routes (Transfers)
        List<RouteResponse.CombinedRoute> combinedRoutes;

        if (!directRoutes.isEmpty()) {
            // Special Logic: If Direct Bus exists, only show Multi-modal if it uses METRO
            // Strategy: Start -> Nearest Metro -> Metro -> Nearest Metro -> End
            RouteResponse.CombinedRoute metroRoute = findBestMetroRoute(graph, startNode, endNode);
            combinedRoutes = metroRoute != null ? Collections.singletonList(metroRoute) : new ArrayList<>();
        } else {
            // Standard Logic: Find any combined route
            combinedRoutes = findCombinedRoutes(graph, startNode, endNode);
        }

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

    // Finds a combined route that MUST use Metro for the middle leg
    private RouteResponse.CombinedRoute findBestMetroRoute(Graph graph, UUID start, UUID end) {
        // 1. Find nearest metro station to Start
        MetroPathSegment startToMetro = findNearestMetro(graph, start);
        // 2. Find nearest metro station to End
        MetroPathSegment endToMetro = findNearestMetro(graph, end);

        if (startToMetro == null || endToMetro == null) {
            return null; // No metro access
        }

        UUID metroStartNode = startToMetro.metroNode;
        UUID metroEndNode = endToMetro.metroNode;

        if (metroStartNode.equals(metroEndNode)) {
            return null; // Start and End are closest to the SAME station, no need to take metro between
                         // them
        }

        // 3. Find path between these two metro stations strictly using METRO
        List<Edge> metroLegEdges = findMetroPath(graph, metroStartNode, metroEndNode);
        if (metroLegEdges.isEmpty()) {
            return null; // These two metro stations are not connected
        }

        // 4. Final Leg: MetroEnd -> End
        List<Edge> lastLegEdges = findSimplePath(graph, metroEndNode, end);
        // IMPORTANT: If start==end, findSimplePath returns empty list (valid).
        // If no path found, it returns null.
        if (lastLegEdges == null) {
            return null;
        }

        List<Edge> fullPath = new ArrayList<>();
        fullPath.addAll(startToMetro.path);
        fullPath.addAll(metroLegEdges);
        fullPath.addAll(lastLegEdges);

        return convertToCombinedRoute(graph, fullPath, start);
    }

    private MetroPathSegment findNearestMetro(Graph graph, UUID startNode) {
        // Priority Queue to prioritize paths with FEWER TRANSFERS, then FEWER STOPS
        Comparator<PathState> comparator = Comparator
                .comparingInt((PathState p) -> p.transfers)
                .thenComparingInt(p -> p.path.size());

        PriorityQueue<PathState> pq = new PriorityQueue<>(comparator);

        // Init state
        // We need to track 'lastTransport' to count transfers
        // Start node has no transport yet, so transfers=0
        pq.add(new PathState(startNode, null, new ArrayList<>(), 0));

        // Visited map: NodeID -> BestTransfersCount
        // We only revisit a node if we found a way with FEWER transfers
        Map<UUID, Integer> visited = new HashMap<>();
        visited.put(startNode, 0);

        int limit = 20000;

        while (!pq.isEmpty() && limit-- > 0) {
            PathState current = pq.poll();

            // If we found a metro station, return it immediately (since PQ ensures best)
            if (isMetroStation(graph, current.node)) {
                return new MetroPathSegment(current.node, current.path);
            }

            // Optimization: If current transfers > 1, stop searching for "Nearest Metro"
            // We only want a simple feeder bus, not a complex journey just to get to metro.
            if (current.transfers > 1)
                continue;

            for (Edge e : graph.getNeighbors(current.node)) {
                boolean isTransfer = current.lastTransport != null && !current.lastTransport.equals(e.getBusName());
                int newTransfers = current.transfers + (isTransfer ? 1 : 0);

                // Pruning
                if (visited.containsKey(e.getStopId()) && visited.get(e.getStopId()) <= newTransfers) {
                    continue;
                }

                visited.put(e.getStopId(), newTransfers);

                List<Edge> newPath = new ArrayList<>(current.path);
                newPath.add(e);

                pq.add(new PathState(e.getStopId(), e.getBusName(), newPath, newTransfers));
            }
        }
        return null;
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

    private List<Edge> reversePath(Graph graph, List<Edge> originalPath) {
        List<Edge> reversed = new ArrayList<>();
        // original: A->B, B->C
        // needed: C->B, B->A

        // We need to find the reverse edge in the graph for each step
        // Iterate backwards
        for (int i = originalPath.size() - 1; i >= 0; i--) {
            Edge fwd = originalPath.get(i);
            // fwd goes from U to V. We need edge from V to U with same transport properties
            // ideally
            // or just any edge if we are walking/bus.
            // Actually, "Bikalpa" A->B might be "Bikalpa" B->A.
            UUID u = getSourceNodeOfEdge(graph, fwd); // Complex because Edge doesn't store source
            // Wait, we can track source from previous iteration or index.

            // Optimization: Re-run BFS/Pathfind from MetroEnd to Destination?
            // "endToMetro" found path End -> Metro.
            // We need path Metro -> End.
            // Since graph is undirected (bidirectional edges exist), we can just find edge
            // V->U.
            // But we don't know U easily from Edge object alone unless we tracked it.
            // Easier approach: Re-calculate path from MetroEnd to Destination using BFS.
        }
        // Fallback: Just re-calculate path simple BFS
        // We know start and end points of this segment.
        // Actually, let's change `findNearestMetro` to return just the MetroNode, and
        // then we calculate path Start->Metro.
        // And for the end leg, we calculate Metro->End.
        return new ArrayList<>(); // Placeholder, will fix in logic
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
