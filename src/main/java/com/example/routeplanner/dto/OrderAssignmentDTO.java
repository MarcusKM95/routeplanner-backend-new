package com.example.routeplanner.dto;

public record OrderAssignmentDTO(
        OrderDTO order,
        CourierDTO courier
) {
}