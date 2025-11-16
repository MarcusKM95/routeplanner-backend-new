package com.example.routeplanner.dto;


public record GridCellDTO(
        int x,
        int y,
        boolean obstacle,
        double weight
) {
}
