package com.suto.controller;

import com.suto.base.BaseController;
import com.suto.dto.RouteRequest;
import com.suto.dto.RouteResponse;
import com.suto.service.PathFinderService;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;

/**
 * Route Controller - Demonstrates Inheritance
 * Extends BaseController to inherit common HTTP handling methods
 */
public class RouteController extends BaseController {

    private final PathFinderService pathFinderService;

    public RouteController(PathFinderService pathFinderService) {
        this.pathFinderService = pathFinderService;
    }

    public void handleFindRoute(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            RouteRequest request = objectMapper.readValue(body, RouteRequest.class);

            if (request.getSource() == null || request.getDestination() == null) {
                sendErrorResponse(exchange, 400, "Origin and Destination are required");
                return;
            }

            RouteResponse response = pathFinderService.findBestRoute(
                    request.getSource(),
                    request.getDestination());

            sendJsonResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
