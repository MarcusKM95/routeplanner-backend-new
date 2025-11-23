package com.example.routeplanner.dto;

import java.util.List;

public record CourierOverviewDTO(
        CourierDTO courier,
        List<OrderDTO> orders,
        MultiStopRouteResponse route
) {
}
