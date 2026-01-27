package com.suto.repository;

import com.suto.model.Booking;
import java.util.List;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);

    List<Booking> findByUserId(UUID userId);

    List<Booking> findByBusId(UUID busId);

    List<Booking> findByDriverId(UUID driverId);

    void updateStatus(UUID bookingId, String status);
}
