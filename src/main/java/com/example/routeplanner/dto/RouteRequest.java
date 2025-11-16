package com.example.routeplanner.dto;

import java.util.List;

/**
 * Input object for requesting a route.
 *
 * Now supports:
 * - basic grid dimensions
 * - start/end coordinates
 * - heuristic choice
 * - optional list of cell overrides (obstacles / weights)
 *
 * Example JSON:
 * {
 *   "gridWidth": 20,
 *   "gridHeight": 20,
 *   "startX": 1,
 *   "startY": 1,
 *   "endX": 15,
 *   "endY": 10,
 *   "heuristic": "MANHATTAN",
 *   "cells": [
 *     { "x": 5, "y": 5, "obstacle": true,  "weight": 1.0 },
 *     { "x": 7, "y": 5, "obstacle": false, "weight": 5.0 }
 *   ]
 * }
 */
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
