package com.example.routeplanner.controller;

import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.RestaurantDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final CityMap cityMap;

    public RestaurantController(CityMap cityMap) {
        this.cityMap = cityMap;
    }

    @GetMapping("/restaurants")
    public List<RestaurantDTO> getRestaurants() {
        return cityMap.getRestaurants().stream()
                .map(r -> new RestaurantDTO(
                        r.id(),
                        r.name(),
                        r.x(),
                        r.y()
                ))
                .toList();
    }
}
