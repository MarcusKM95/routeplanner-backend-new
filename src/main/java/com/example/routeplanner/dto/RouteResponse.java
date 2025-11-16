package com.example.routeplanner.dto;
import java.util.List;


public record RouteResponse(
        List<PointDTO> path,
        double totalDistance,
        int visitedNodes,
        long timeMs
) {
}
