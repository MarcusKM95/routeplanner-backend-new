package com.example.routeplanner.dto;

import java.util.List;

public record CourierDTO(
        String id,
        String name,
        int currentX,
        int currentY,
        List<Long> assignedOrderIds
) {
}
