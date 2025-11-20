package com.example.routeplanner.controller;
import com.example.routeplanner.dto.DeliveryStopDTO;
import com.example.routeplanner.dto.PointDTO;
import com.example.routeplanner.dto.MultiStopRouteRequest;
import com.example.routeplanner.dto.MultiStopRouteResponse;
import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.RouteFromRestaurantRequest;
import com.example.routeplanner.dto.RouteRequest;
import com.example.routeplanner.dto.RouteResponse;
import com.example.routeplanner.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // Multi-stop route endpoint that chains multiple legs together using the city map's grid.
    @PostMapping("/route/multi")
    public ResponseEntity<?> multiStopRoute(@RequestBody MultiStopRouteRequest req) {
        try {
            if (req.restaurantId() == null || req.restaurantId().isBlank()) {
                return ResponseEntity.badRequest().body("restaurantId is required");
            }

            if (req.stops() == null || req.stops().isEmpty()) {
                return ResponseEntity.badRequest().body("At least one stop is required");
            }

            var restaurantOpt = cityMap.findRestaurantById(req.restaurantId());
            if (restaurantOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Unknown restaurant id: " + req.restaurantId());
            }

            var restaurant = restaurantOpt.get();
            var grid = cityMap.getGrid();
            String heuristic = req.heuristic();

            int currentX = restaurant.x();
            int currentY = restaurant.y();

            List<PointDTO> fullPath = new java.util.ArrayList<>();
            double totalDistance = 0.0;
            int totalVisitedNodes = 0;
            long totalTimeMs = 0L;

            boolean firstLeg = true;

            for (DeliveryStopDTO stop : req.stops()) {
                int targetX = stop.x();
                int targetY = stop.y();

                if (!grid.inBounds(targetX, targetY)) {
                    return ResponseEntity.badRequest()
                            .body("Stop out of bounds: (" + targetX + ", " + targetY + ")");
                }

                var legResponse = routeService.computeRouteOnGrid(
                        grid,
                        currentX,
                        currentY,
                        targetX,
                        targetY,
                        heuristic
                );

                totalVisitedNodes += legResponse.visitedNodes();
                totalTimeMs += legResponse.timeMs();

                if (!Double.isFinite(legResponse.totalDistance()) || legResponse.path().isEmpty()) {
                    //Route is impossible
                    totalDistance = Double.POSITIVE_INFINITY;
                    break;
                }

                totalDistance += legResponse.totalDistance();

                var legPath = legResponse.path();

                if (firstLeg) {
                    fullPath.addAll(legPath);
                    firstLeg = false;
                } else {
                    for (int i = 1; i < legPath.size(); i++) {
                        fullPath.add(legPath.get(i));
                    }
                }

                currentX = targetX;
                currentY = targetY;
            }

            MultiStopRouteResponse response = new MultiStopRouteResponse(
                    fullPath,
                    totalDistance,
                    totalVisitedNodes,
                    totalTimeMs
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + ex.getMessage());
        }
    }


}

