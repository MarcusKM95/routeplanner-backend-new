package com.example.routeplanner.controller;

import com.example.routeplanner.dto.CreateOrderRequest;
import com.example.routeplanner.dto.OrderDTO;
import com.example.routeplanner.service.OrderService;
import com.example.routeplanner.dto.OrderAssignmentDTO;
import com.example.routeplanner.service.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final DispatchService dispatchService;

    public OrderController(OrderService orderService, DispatchService dispatchService) {
        this.orderService = orderService;
        this.dispatchService = dispatchService;
    }

    // Create a new order
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest req) {
        try {
            OrderDTO dto = orderService.createOrder(req);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + ex.getMessage());
        }
    }

    // List all orders
    @GetMapping
    public List<OrderDTO> list() {
        return orderService.listOrders();
    }

    // Get one order by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable("id") long id) {
        try {
            OrderDTO dto = orderService.getOrder(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + ex.getMessage());
        }
    }

    // Assign the given order to the best courier
    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assign(@PathVariable("id") long id) {
        try {
            OrderAssignmentDTO assignment = dispatchService.assignOrderToBestCourier(id);
            return ResponseEntity.ok(assignment);
        } catch (IllegalArgumentException ex) {
            // e.g. order not found, or not NEW
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            // e.g. no couriers available, or no suitable courier
            return ResponseEntity.status(409).body(ex.getMessage());
        } catch (Exception ex) {
            // fallback: anything unexpected
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + ex.getMessage());
        }
    }



}
