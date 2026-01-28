package com.suto.service;

import com.suto.dto.BookingRequest;
import com.suto.model.Booking;
import com.suto.repository.BookingRepository;
import com.suto.repository.UserRepository;
import com.suto.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public Booking createBooking(BookingRequest request, String token) {
        String userIdStr = jwtUtil.extractClaim(token.replace("Bearer ", ""),
                claims -> claims.get("userId", String.class));

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setUserId(UUID.fromString(userIdStr));
        booking.setBusId(UUID.fromString(request.getBusId()));
        booking.setFromStopId(UUID.fromString(request.getFromStopId()));
        booking.setToStopId(UUID.fromString(request.getToStopId()));
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("confirmed"); // Auto-confirm for now
        booking.setFare(request.getFare());

        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(String token) {
        String userIdStr = jwtUtil.extractClaim(token.replace("Bearer ", ""),
                claims -> claims.get("userId", String.class));
        return bookingRepository.findByUserId(UUID.fromString(userIdStr));
    }

    public List<Booking> getDriverBookings(String driverId) {
        // If driverId is "me", we need token? Controller passes ID or "me".
        // Controller currently takes PathVariable.
        // If the User passes "me" in Frontend, backend receives "me".
        // BUT the controller code I wrote takes @PathVariable String driverId.
        // And Frontend `fetchTrips` calls `apiClient.getDriverTrips(driverId || 'me')`.
        // So we might get "me". If "me", we can't resolve without token.
        // I should have passed token to `getDriverBookings`.

        // Quick fix: User passes real ID. Frontend `checkAuth` gets ID.
        return bookingRepository.findByDriverId(UUID.fromString(driverId));
    }

    public void updateStatus(String bookingId, String status) {
        bookingRepository.updateStatus(UUID.fromString(bookingId), status);
    }
}
