package com.suto.controller;

import com.suto.dto.BookingRequest;
import com.suto.model.Booking;
import com.suto.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            Booking booking = bookingService.createBooking(request, token);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating booking: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserBookings(@RequestHeader("Authorization") String token) {
        try {
            List<Booking> bookings = bookingService.getUserBookings(token);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching bookings: " + e.getMessage());
        }
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getDriverBookings(@PathVariable String driverId) {
        try {
            // Logic to fetch bookings for a driver
            // For 'me', we might need to resolve ID from token, but here we accept ID.
            List<Booking> bookings = bookingService.getDriverBookings(driverId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching driver bookings: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable String id,
            @RequestBody java.util.Map<String, String> payload) {
        try {
            String status = payload.get("status");
            bookingService.updateStatus(id, status);
            return ResponseEntity.ok("{\"message\": \"Status updated\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating status: " + e.getMessage());
        }
    }
}
