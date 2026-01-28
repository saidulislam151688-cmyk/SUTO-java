package com.suto;

import com.suto.controller.*;
import com.suto.repository.*;
import com.suto.security.JwtUtil;
import com.suto.service.*;
import com.suto.util.JsonFileService;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * Main Application Entry Point - Replaces Spring Boot
 */
public class Application {

    private static AuthController authController;
    private static RouteController routeController;
    private static BookingController bookingController;
    private static UserController userController;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting SUTO Java Application...");

        // Initialize dependencies manually
        initializeDependencies();

        // Create HTTP Server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Register routes
        server.createContext("/api/auth/signup", new SignupHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/routes/find", new RouteFindHandler());
        server.createContext("/api/bookings", new BookingsHandler());
        server.createContext("/api/users/me", new UserMeHandler());

        server.setExecutor(null); // Use default executor
        server.start();

        System.out.println("Server started on http://localhost:8080");
    }

    private static void initializeDependencies() {
        // Utilities
        JsonFileService jsonFileService = new JsonFileService();

        // Repositories
        JsonUserRepository userRepository = new JsonUserRepository(jsonFileService);
        JsonBookingRepository bookingRepository = new JsonBookingRepository(jsonFileService);

        // Services
        GraphService graphService = new GraphService();
        PathFinderService pathFinderService = new PathFinderService(graphService);
        JwtUtil jwtUtil = new JwtUtil();

        AuthService authService = new AuthService(userRepository, jwtUtil);
        UserService userService = new UserService(userRepository, jwtUtil);
        BookingService bookingService = new BookingService(bookingRepository, userRepository, jwtUtil);

        // Controllers
        authController = new AuthController(authService);
        routeController = new RouteController(pathFinderService);
        bookingController = new BookingController(bookingService);
        userController = new UserController(userService);

        System.out.println("All dependencies initialized");
    }

    // Handler classes for each endpoint
    static class SignupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            authController.handleSignup(exchange);
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            authController.handleLogin(exchange);
        }
    }

    static class RouteFindHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            routeController.handleFindRoute(exchange);
        }
    }

    static class BookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            String method = exchange.getRequestMethod();
            if ("POST".equals(method)) {
                bookingController.handleCreateBooking(exchange);
            } else if ("GET".equals(method)) {
                bookingController.handleGetBookings(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class UserMeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            userController.handleGetProfile(exchange);
        }
    }

    private static void handleCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
