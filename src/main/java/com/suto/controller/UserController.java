package com.suto.controller;

import com.suto.base.BaseController;
import com.suto.service.UserService;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;

/**
 * User Controller - Demonstrates Inheritance
 * Extends BaseController to inherit common HTTP handling methods
 */
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void handleGetProfile(HttpExchange exchange) throws IOException {
        try {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            Object profile = userService.getProfile(token);
            sendJsonResponse(exchange, 200, profile);
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Unauthorized");
        }
    }
}
