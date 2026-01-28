package com.suto.controller;

import com.suto.base.BaseController;
import com.suto.dto.AuthResponse;
import com.suto.dto.LoginRequest;
import com.suto.dto.SignupRequest;
import com.suto.service.AuthService;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;

/**
 * Authentication Controller - Demonstrates Inheritance
 * Extends BaseController to inherit common HTTP handling methods
 */
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void handleSignup(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            SignupRequest request = objectMapper.readValue(body, SignupRequest.class);

            AuthResponse response = authService.signup(request);
            sendJsonResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        }
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            LoginRequest request = objectMapper.readValue(body, LoginRequest.class);

            AuthResponse response = authService.login(request);
            sendJsonResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendErrorResponse(exchange, 401, e.getMessage());
        }
    }

    public void handleGetProfile(HttpExchange exchange) throws IOException {
        try {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            Object profile = authService.getUserProfile(token);
            sendJsonResponse(exchange, 200, profile);
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Invalid Token");
        }
    }
}
