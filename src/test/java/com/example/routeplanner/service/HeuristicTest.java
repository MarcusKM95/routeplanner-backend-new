package com.example.routeplanner.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeuristicTest {

    @Test
    void manhattan_returnsSumOfAbsoluteDifferences() {
        Heuristic h = Heuristic.MANHATTAN;

        double d1 = h.estimate(0, 0, 3, 4);   // |3-0| + |4-0| = 7
        double d2 = h.estimate(5, 5, 2, 1);   // |2-5| + |1-5| = 7

        assertEquals(7.0, d1, 1e-9);
        assertEquals(7.0, d2, 1e-9);
    }

    @Test
    void euclidean_matchesPythagoreanDistance() {
        Heuristic h = Heuristic.EUCLIDEAN;

        double d = h.estimate(0, 0, 3, 4);  // sqrt(3^2 + 4^2) = 5

        assertEquals(5.0, d, 1e-9);
    }

    @Test
    void none_returnsZeroForAnyPoints() {
        Heuristic h = Heuristic.NONE;

        assertEquals(0.0, h.estimate(0, 0, 0, 0), 1e-9);
        assertEquals(0.0, h.estimate(10, -5, -3, 100), 1e-9);
    }
}
