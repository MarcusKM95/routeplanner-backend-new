package com.example.routeplanner.dto;

import java.util.List;


public record RouteRequest(
        int gridWidth,
        int gridHeight,
        int startX,
        int startY,
        int endX,
        int endY,
        String heuristic,
        List<GridCellDTO> cells // optional: can be null or empty
) {
}
