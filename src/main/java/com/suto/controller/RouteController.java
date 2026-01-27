package com.suto.controller;

import com.suto.dto.RouteRequest;
import com.suto.dto.RouteResponse;
import com.suto.service.PathFinderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*") // Allow frontend access
public class RouteController {

    @Autowired
    private PathFinderService pathFinderService;

    @PostMapping("/find")
    public ResponseEntity<?> findRoute(@RequestBody RouteRequest request) {
        try {
            if (request.getOrigin() == null || request.getDestination() == null) {
                return ResponseEntity.badRequest().body("Origin and Destination are required");
            }

            RouteResponse response = pathFinderService.findBestRoute(
                    request.getOrigin().trim(),
                    request.getDestination().trim());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }
}
