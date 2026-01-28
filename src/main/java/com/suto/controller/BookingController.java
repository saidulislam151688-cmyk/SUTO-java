package com.suto.controller;

import com.suto.base.BaseController;
import com.suto.dto.BookingRequest;
import com.suto.model.Booking;
import com.suto.service.BookingService;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Booking Controller - Demonstrates Inheritance
 * Extends BaseController to inherit common HTTP handling methods
 */
public class BookingController extends BaseController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public void handleCreateBooking(HttpExchange exchange) throws IOException {
        try {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            String body = readRequestBody(exchange);
            BookingRequest request = objectMapper.readValue(body, BookingRequest.class);

            Booking booking = bookingService.createBooking(request, token);
            sendJsonResponse(exchange, 200, booking);
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error creating booking: " + e.getMessage());
        }
    }

    public void handleGetBookings(HttpExchange exchange) throws IOException {
        try {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            List<Booking> bookings = bookingService.getUserBookings(token);
            sendJsonResponse(exchange, 200, bookings);
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error fetching bookings");
        }
    }
}
