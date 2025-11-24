package com.example.routeplanner.service;

// Heuristic functions used by A* to estimate the remaining distance from a node to the goal.
public enum Heuristic {
    MANHATTAN,
    EUCLIDEAN,
    NONE;

    //Calculate the heuristic distance between (x1, y1) and (x2, y2).

    public double estimate(int x1, int y1, int x2, int y2) {
        return switch (this) {
            case MANHATTAN -> Math.abs(x1 - x2) + Math.abs(y1 - y2);
            case EUCLIDEAN -> Math.hypot(x1 - x2, y1 - y2);
            case NONE -> 0.0;
        };
    }
}
