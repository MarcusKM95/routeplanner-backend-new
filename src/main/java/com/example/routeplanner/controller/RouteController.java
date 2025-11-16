package com.example.routeplanner.controller;

import com.example.routeplanner.dto.RouteRequest;
import com.example.routeplanner.dto.RouteResponse;
import com.example.routeplanner.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping("/route")
    public ResponseEntity<?> computeRoute(@RequestBody RouteRequest request) {
        try {
            RouteResponse response = routeService.computeRoute(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // For bad input (e.g., start outside grid)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Catch-all (we can improve later)
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }
}
