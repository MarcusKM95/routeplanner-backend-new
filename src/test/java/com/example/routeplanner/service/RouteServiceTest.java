package com.example.routeplanner.service;

import com.example.routeplanner.dto.PointDTO;
import com.example.routeplanner.dto.RouteResponse;
import com.example.routeplanner.model.Grid;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteServiceTest {

    private final RouteService routeService = new RouteService();

    @Test
    void computesStraightLinePathOnEmptyGrid_manhattan() {
        Grid grid = new Grid(5, 5);

        // From (0,0) to (4,0) – should move in a straight line, 4 steps
        RouteResponse res = routeService.computeRouteOnGrid(
                grid,
                0, 0,
                4, 0,
                "MANHATTAN"
        );

        // Distance is number of steps * weight(=1) -> 4
        assertEquals(4.0, res.totalDistance(), 1e-9, "Total distance should be 4 on an empty grid");

        List<PointDTO> path = res.path();
        assertEquals(5, path.size(), "Path should contain 5 points (including start and end)");
        assertEquals(new PointDTO(0, 0), path.get(0));
        assertEquals(new PointDTO(4, 0), path.get(path.size() - 1));

        assertTrue(res.visitedNodes() > 0, "Algorithm should visit at least one node");
    }

    @Test
    void returnsInfiniteDistanceAndEmptyPathWhenBlockedByObstacleWall() {
        Grid grid = new Grid(5, 5);

        // Create a horizontal wall at y = 1 from x=0..4,
        // so there is no way to go from (0,0) to (0,2)
        for (int x = 0; x < 5; x++) {
            grid.setObstacle(x, 1, true);
        }

        RouteResponse res = routeService.computeRouteOnGrid(
                grid,
                0, 0,
                0, 2,
                "MANHATTAN"
        );

        assertTrue(Double.isInfinite(res.totalDistance()),
                "When no path exists, totalDistance should be infinite");
        assertTrue(res.path().isEmpty(), "When no path exists, path should be empty");
        assertTrue(res.visitedNodes() > 0, "Algorithm should still visit some nodes");
    }

    @Test
    void throwsExceptionWhenStartIsOutOfBounds() {
        Grid grid = new Grid(5, 5);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> routeService.computeRouteOnGrid(
                        grid,
                        -1, 0,   // invalid start
                        2, 2,
                        "MANHATTAN"
                )
        );

        assertTrue(ex.getMessage().contains("Start position"),
                "Exception message should mention start position");
    }

    @Test
    void throwsExceptionWhenEndIsOutOfBounds() {
        Grid grid = new Grid(5, 5);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> routeService.computeRouteOnGrid(
                        grid,
                        0, 0,
                        10, 0,  // invalid end
                        "MANHATTAN"
                )
        );

        assertTrue(ex.getMessage().contains("End position"),
                "Exception message should mention end position");
    }

    @Test
    void invalidHeuristicNameFallsBackToManhattan() {
        Grid grid = new Grid(5, 5);

        // Using a bogus heuristic name should behave like MANHATTAN
        RouteResponse res = routeService.computeRouteOnGrid(
                grid,
                0, 0,
                4, 0,
                "NOT_A_REAL_HEURISTIC"
        );

        // Same expectations as the manhattan straight-line test
        assertEquals(4.0, res.totalDistance(), 1e-9);
        assertEquals(5, res.path().size());
        assertEquals(new PointDTO(0, 0), res.path().get(0));
        assertEquals(new PointDTO(4, 0), res.path().get(res.path().size() - 1));
    }
}
