package com.example.routeplanner.service;

import com.example.routeplanner.dto.PointDTO;
import com.example.routeplanner.dto.RouteRequest;
import com.example.routeplanner.dto.RouteResponse;
import com.example.routeplanner.model.Grid;
import com.example.routeplanner.model.Node;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Core routing logic using the A* algorithm for a single A -> B route.
 */
@Service
public class RouteService {

    /**
     * Compute a route on a simple grid using A*.
     * For now:
     * - Grid has uniform weight = 1.0 everywhere
     * - No obstacles
     */
    public RouteResponse computeRoute(RouteRequest request) {
        // --- 1) Basic validation ---
        int width = request.gridWidth();
        int height = request.gridHeight();

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Grid width and height must be > 0");
        }

        Grid grid = new Grid(width, height);

// Apply optional cell overrides (obstacles / weights) from the request
        if (request.cells() != null) {
            request.cells().forEach(cell -> {
                int x = cell.x();
                int y = cell.y();
                if (!grid.inBounds(x, y)) {
                    // ignore out-of-bounds cells silently
                    return;
                }

                // set obstacle flag
                grid.setObstacle(x, y, cell.obstacle());

                // set weight (even for obstacles; weight won't matter if blocked)
                grid.setWeight(x, y, cell.weight());
            });
        }

        int startX = request.startX();
        int startY = request.startY();
        int endX = request.endX();
        int endY = request.endY();


        if (!grid.inBounds(startX, startY)) {
            throw new IllegalArgumentException("Start position is outside the grid");
        }
        if (!grid.inBounds(endX, endY)) {
            throw new IllegalArgumentException("End position is outside the grid");
        }

        // Parse heuristic string, default to MANHATTAN if unknown
        Heuristic heuristic = parseHeuristic(request.heuristic());

        // --- 2) Setup A* search structures ---
        Node[][] nodes = new Node[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodes[y][x] = new Node(x, y);
            }
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Node startNode = nodes[startY][startX];
        Node goalNode = nodes[endY][endX];

        startNode.setGCost(0.0);
        startNode.setHCost(heuristic.estimate(startX, startY, endX, endY));
        openSet.add(startNode);

        int visitedNodes = 0;
        long startTimeNs = System.nanoTime();

        // 4-directional moves: right, left, down, up
        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        // --- 3) Main A* loop ---
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.isClosed()) {
                continue; // already processed with a better cost
            }
            current.setClosed(true);
            visitedNodes++;

            // If we reached the goal, stop
            if (current == goalNode) {
                break;
            }

            for (int[] dir : directions) {
                int nx = current.getX() + dir[0];
                int ny = current.getY() + dir[1];

                // outside grid?
                if (!grid.inBounds(nx, ny)) {
                    continue;
                }

                // blocked cell?
                if (grid.isObstacle(nx, ny)) {
                    continue;
                }

                Node neighbor = nodes[ny][nx];

                // movement cost: depends on cell weight
                double tentativeG = current.getGCost() + grid.getWeight(nx, ny);

                if (tentativeG < neighbor.getGCost()) {
                    neighbor.setGCost(tentativeG);
                    neighbor.setHCost(heuristic.estimate(nx, ny, endX, endY));
                    neighbor.setParent(current);
                    openSet.add(neighbor);
                }
            }

        }

        long endTimeNs = System.nanoTime();
        long timeMs = (endTimeNs - startTimeNs) / 1_000_000;

        // --- 4) Reconstruct path ---
        List<PointDTO> pathPoints = new ArrayList<>();

        if (!goalNode.isClosed()) {
            // No path found
            return new RouteResponse(
                    pathPoints,   // empty path
                    Double.POSITIVE_INFINITY,
                    visitedNodes,
                    timeMs
            );
        }

        Node current = goalNode;
        while (current != null) {
            pathPoints.add(new PointDTO(current.getX(), current.getY()));
            current = current.getParent();
        }
        Collections.reverse(pathPoints);

        double totalDistance = goalNode.getGCost(); // since each step cost = weight (currently 1.0)

        return new RouteResponse(
                pathPoints,
                totalDistance,
                visitedNodes,
                timeMs
        );
    }

    /**
     * Convert a string from the request into a Heuristic enum.
     * Defaults to MANHATTAN if null or unknown.
     */
    private Heuristic parseHeuristic(String heuristicName) {
        if (heuristicName == null) {
            return Heuristic.MANHATTAN;
        }
        try {
            return Heuristic.valueOf(heuristicName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Heuristic.MANHATTAN;
        }
    }
}
