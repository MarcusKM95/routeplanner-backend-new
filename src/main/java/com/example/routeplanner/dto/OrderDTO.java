package com.example.routeplanner.dto;

// Data transfer object for exposing orders via the API.

public record OrderDTO(
        long id,
        String restaurantId,
        int x,
        int y,
        String label,
        String status,
        String assignedCourierId
) {
}

