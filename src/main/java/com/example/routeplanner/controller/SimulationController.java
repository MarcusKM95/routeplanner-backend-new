package com.example.routeplanner.controller;

import com.example.routeplanner.service.CourierService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sim")
@CrossOrigin(origins = "*")
public class SimulationController {

    private final CourierService courierService;

    public SimulationController(CourierService courierService) {
        this.courierService = courierService;
    }

    @PostMapping("/step")
    public void step() {
        courierService.stepAllCouriers();
    }
}
