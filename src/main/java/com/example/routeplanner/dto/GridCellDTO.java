package com.example.routeplanner.dto;

/**
 * Describes a single cell in the grid as sent from the client.
 *
 * This allows the frontend to override:
 * - whether the cell is an obstacle
 * - the movement weight (cost) of the cell
 *
 * Example JSON:
 * { "x": 5, "y": 7, "obstacle": true, "weight": 1.0 }
 */
public record GridCellDTO(
        int x,
        int y,
        boolean obstacle,
        double weight
) {
}
