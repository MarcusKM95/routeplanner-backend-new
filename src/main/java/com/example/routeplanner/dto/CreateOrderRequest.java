package com.example.routeplanner.dto;

// Data transfer object for creating a new order via the API.
public record CreateOrderRequest(
        String restaurantId,
        int x,
        int y,
        String label
) {
}