package com.example.routeplanner.controller;

import com.example.routeplanner.dto.CourierDTO;
import com.example.routeplanner.service.CourierService;
import com.example.routeplanner.dto.MultiStopRouteResponse;
import com.example.routeplanner.service.CourierRouteService;
import com.example.routeplanner.dto.CourierOverviewDTO;
import com.example.routeplanner.dto.OrderDTO;
import com.example.routeplanner.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/couriers")
@CrossOrigin(origins = "*")
public class CourierController {

    private final CourierService courierService;
    private final CourierRouteService courierRouteService;
    private final OrderService orderService;

    public CourierController(CourierService courierService,
                             CourierRouteService courierRouteService, OrderService orderService) {
        this.courierService = courierService;
        this.courierRouteService = courierRouteService;
        this.orderService = orderService;
    }

    // List all couriers
    @GetMapping
    public List<CourierDTO> list() {
        return courierService.listCouriers();
    }

    @GetMapping("/{id}/route")
    public ResponseEntity<?> getCourierRoute(
            @PathVariable("id") String id,
            @RequestParam(name = "heuristic", defaultValue = "MANHATTAN") String heuristic,
            @RequestParam(name = "strategy", defaultValue = "NEAREST_NEIGHBOR") String strategy
    ) {
        try {
            MultiStopRouteResponse route =
                    courierRouteService.computeRouteForCourier(id, heuristic, strategy);
            return ResponseEntity.ok(route);
        } catch (IllegalArgumentException ex) {
            //unknown courier id or invalid stop
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + ex.getMessage());
        }
    }

    @GetMapping("/{id}/overview")
    public ResponseEntity<?> getCourierOverview(
            @PathVariable("id") String id,
            @RequestParam(name = "heuristic", defaultValue = "MANHATTAN") String heuristic,
            @RequestParam(name = "strategy", defaultValue = "NEAREST_NEIGHBOR") String strategy
    ) {
        try {
            // Get courier entity
            var courierEntity = courierService.getCourier(id);
            if (courierEntity == null) {
                return ResponseEntity.badRequest().body("Unknown courier id: " + id);
            }

            // Convert courier to DTO
            var courierDTO = courierService.toDTOPublic(courierEntity);

            // Collect assigned orders as DTOs
            List<OrderDTO> orderDTOs = courierEntity.getAssignedOrderIds().stream()
                    .map(orderService::getOrder)
                    .toList();

            // make route for this courier
            MultiStopRouteResponse route =
                    courierRouteService.computeRouteForCourier(id, heuristic, strategy);

            CourierOverviewDTO overview = new CourierOverviewDTO(
                    courierDTO,
                    orderDTOs,
                    route
            );

            return ResponseEntity.ok(overview);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + ex.getMessage());
        }
    }
}
