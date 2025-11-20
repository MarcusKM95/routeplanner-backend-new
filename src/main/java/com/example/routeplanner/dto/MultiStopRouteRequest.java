package com.example.routeplanner.dto;
import java.util.List;

public record MultiStopRouteRequest(
        String restaurantId,        // start from this restaurant
        List<DeliveryStopDTO> stops,
        String heuristic,           // "MANHATTAN" or "EUCLIDEAN"
        String strategy             // "IN_ORDER" now, "NEAREST_NEIGHBOR" later
) {
}
