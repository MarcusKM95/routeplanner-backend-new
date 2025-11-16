package com.example.routeplanner.service;

import com.example.routeplanner.dto.PointDTO;
import com.example.routeplanner.dto.RouteRequest;
import com.example.routeplanner.dto.RouteResponse;
import com.example.routeplanner.dto.GridCellDTO;
import com.example.routeplanner.model.Grid;
import com.example.routeplanner.model.Node;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;


@Service
public class RouteService {


    public RouteResponse computeRoute(RouteRequest request) {
        int width = request.gridWidth();
        int height = request.gridHeight();

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Grid width and height must be > 0");
        }

        Grid grid = new Grid(width, height);

        // Apply optional cell overrides (obstacles / weights) from the request
        if (request.cells() != null) {
            applyCellsToGrid(grid, request.cells());
        }

        // Delegate to the new, grid-based method
        return computeRouteOnGrid(
                grid,
                request.startX(),
                request.startY(),
                request.endX(),
                request.endY(),
                request.heuristic()
        );
    }

    public RouteResponse computeRouteOnGrid(
            Grid grid,
            int startX,
            int startY,
            int endX,
            int endY,
            String heuristicName
    ) {
        if (grid == null) {
            throw new IllegalArgumentException("Grid must not be null");
        }

        if (!grid.inBounds(startX, startY)) {
            throw new IllegalArgumentException("Start position is outside the grid");
        }
        if (!grid.inBounds(endX, endY)) {
            throw new IllegalArgumentException("End position is outside the grid");
        }

        Heuristic heuristic = parseHeuristic(heuristicName);

        int width = grid.getWidth();
        int height = grid.getHeight();

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

        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.isClosed()) {
                continue;
            }
            current.setClosed(true);
            visitedNodes++;

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

        List<PointDTO> pathPoints = new ArrayList<>();

        if (!goalNode.isClosed()) {
            // No path found
            return new RouteResponse(
                    pathPoints,
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

        double totalDistance = goalNode.getGCost(); // sum of weights used

        return new RouteResponse(
                pathPoints,
                totalDistance,
                visitedNodes,
                timeMs
        );
    }

    private void applyCellsToGrid(Grid grid, List<GridCellDTO> cells) {
        for (GridCellDTO cell : cells) {
            int x = cell.x();
            int y = cell.y();
            if (!grid.inBounds(x, y)) {
                continue; // ignore out-of-bounds
            }

            grid.setObstacle(x, y, cell.obstacle());
            grid.setWeight(x, y, cell.weight());
        }
    }

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
