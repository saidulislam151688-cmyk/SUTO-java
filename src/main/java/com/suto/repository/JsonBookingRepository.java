package com.suto.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suto.model.Booking;
import com.suto.util.JsonFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Primary
public class JsonBookingRepository implements BookingRepository {

    private static final String BOOKINGS_FILE = "bookings.json";

    @Autowired
    private JsonFileService jsonFileService;

    @Override
    public Booking save(Booking booking) {
        List<Booking> bookings = getAllBookings();

        if (booking.getId() == null) {
            booking.setId(UUID.randomUUID());
        }
        if (booking.getCreatedAt() == null) {
            booking.setCreatedAt(LocalDateTime.now());
        }

        // Remove existing if updating
        bookings.removeIf(b -> b.getId().equals(booking.getId()));
        bookings.add(booking);

        jsonFileService.writeList(BOOKINGS_FILE, bookings);
        return booking;
    }

    @Override
    public List<Booking> findByUserId(UUID userId) {
        List<Booking> bookings = getAllBookings();
        return bookings.stream()
                .filter(b -> b.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByBusId(UUID busId) {
        List<Booking> bookings = getAllBookings();
        return bookings.stream()
                .filter(b -> b.getBusId() != null && b.getBusId().equals(busId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByDriverId(UUID driverId) {
        List<Booking> bookings = getAllBookings();
        return bookings.stream()
                .filter(b -> b.getDriverId() != null && b.getDriverId().equals(driverId))
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(UUID bookingId, String status) {
        List<Booking> bookings = getAllBookings();
        for (Booking b : bookings) {
            if (b.getId().equals(bookingId)) {
                b.setStatus(status);
                save(b);
                break;
            }
        }
    }

    private List<Booking> getAllBookings() {
        return jsonFileService.readList(BOOKINGS_FILE, new TypeReference<List<Booking>>() {
        });
    }
}
