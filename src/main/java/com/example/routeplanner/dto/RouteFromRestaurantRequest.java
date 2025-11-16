package com.example.routeplanner.dto;

public record RouteFromRestaurantRequest(
        String restaurantId,
        int endX,
        int endY,
        String heuristic
) {
}