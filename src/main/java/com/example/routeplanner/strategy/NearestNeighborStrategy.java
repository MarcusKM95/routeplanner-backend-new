package com.example.routeplanner.strategy;

import com.example.routeplanner.dto.DeliveryStopDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Each step: pick the closest not-yet-visited stop (by Manhattan distance)

@Component
public class NearestNeighborStrategy implements DeliveryStrategy {

    @Override
    public String getName() {
        return "NEAREST_NEIGHBOR";
    }

    @Override
    public List<DeliveryStopDTO> orderStops(List<DeliveryStopDTO> stops, int startX, int startY) {
        if (stops == null || stops.isEmpty()) {
            return List.of();
        }

        List<DeliveryStopDTO> remaining = new ArrayList<>(stops);
        List<DeliveryStopDTO> ordered = new ArrayList<>(stops.size());

        int currentX = startX;
        int currentY = startY;

        while (!remaining.isEmpty()) {
            DeliveryStopDTO closest = null;
            int bestDist = Integer.MAX_VALUE;

            for (DeliveryStopDTO stop : remaining) {
                int dist = manhattan(currentX, currentY, stop.x(), stop.y());
                if (dist < bestDist) {
                    bestDist = dist;
                    closest = stop;
                }
            }

            ordered.add(closest);
            remaining.remove(closest);

            currentX = closest.x();
            currentY = closest.y();
        }

        return ordered;
    }

    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}
