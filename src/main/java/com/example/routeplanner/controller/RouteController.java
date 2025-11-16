package com.example.routeplanner.controller;

import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.RouteFromRestaurantRequest;
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
    private final CityMap cityMap;

    public RouteController(RouteService routeService, CityMap cityMap) {
        this.routeService = routeService;
        this.cityMap = cityMap;
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
            // Catch-all
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/route/from-restaurant")
    public ResponseEntity<?> routeFromRestaurant(@RequestBody RouteFromRestaurantRequest req) {
        try {
            var restaurantOpt = cityMap.findRestaurantById(req.restaurantId());
            if (restaurantOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Unknown restaurant id: " + req.restaurantId());
            }

            var restaurant = restaurantOpt.get();

            // Use the fixed CityMap grid directly
            var grid = cityMap.getGrid();

            RouteResponse res = routeService.computeRouteOnGrid(
                    grid,
                    restaurant.x(),   // startX from restaurant
                    restaurant.y(),   // startY from restaurant
                    req.endX(),
                    req.endY(),
                    req.heuristic()
            );

            return ResponseEntity.ok(res);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + ex.getMessage());
        }
    }
}

