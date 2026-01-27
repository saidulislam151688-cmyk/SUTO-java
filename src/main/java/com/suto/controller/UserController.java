package com.suto.controller;

import com.suto.model.User;
import com.suto.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/drivers/owner/{ownerId}")
    public ResponseEntity<?> getDriversByOwner(@PathVariable String ownerId) {
        try {
            // For now, if "me" or any ID, just return ALL drivers (as requested/implied by
            // previous "fetch all")
            // Or filter if we had owner-driver relationship.
            // Previous code fetched "/api/owner/drivers" which seemingly returned all.
            List<User> drivers = userService.getAllDrivers();
            // Wrap in map to match frontend expectation { drivers: [] } or array?
            // Frontend: `if (data) setDrivers(data)` (I changed it to array)
            // But let's check my frontend change... I set `setDrivers(data)`.
            // So returning list is fine.
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching drivers: " + e.getMessage());
        }
    }

    // Placeholder for profile fetch if needed
    @GetMapping("/profiles/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable String userId) {
        try {
            // Return mocked profile or fetch from DB
            return ResponseEntity.ok(userService.getUserById(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
