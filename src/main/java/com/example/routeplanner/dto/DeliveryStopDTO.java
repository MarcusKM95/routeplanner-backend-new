package com.example.routeplanner.dto;

public record DeliveryStopDTO(
        int x,
        int y,
        String label // optional descriptive label, may be null
) {
}