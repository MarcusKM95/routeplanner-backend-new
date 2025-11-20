package com.example.routeplanner.strategy;

import com.example.routeplanner.dto.DeliveryStopDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Strategy that delivers stops in the order they are provided
@Component
public class InOrderStrategy implements DeliveryStrategy {

    @Override
    public String getName() {
        return "IN_ORDER";
    }

    @Override
    public List<DeliveryStopDTO> orderStops(List<DeliveryStopDTO> stops, int startX, int startY) {
        // Simply return the stops as they are provided
        return stops == null ? List.of() : new ArrayList<>(stops);
    }
}