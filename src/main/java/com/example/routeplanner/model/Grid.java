package com.example.routeplanner.model;


public class Grid {

    private final int width;
    private final int height;
    private final double[][] weights;   // cost to enter a cell
    private final boolean[][] obstacles; // true = blocked

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;

        this.weights = new double[height][width];
        this.obstacles = new boolean[height][width];

        // initialize all cells as walkable road with weight 1.0
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                weights[y][x] = 1.0;
                obstacles[y][x] = false;
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    //
    // Check if coordinates are inside the grid.
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }


     // Get the movement cost of entering a cell
    public double getWeight(int x, int y) {
        return weights[y][x];
    }

    // Set the movement cost of entering a cell (x, y). And ignore the calls that are out of bounds.
    public void setWeight(int x, int y, double weight) {
        if (!inBounds(x, y)) return;
        if (weight <= 0) weight = 1.0; // keep it sane
        weights[y][x] = weight;
    }

    // Mark a cell as obstacle (true) or walkable (false). Ignore the calls that are out of bounds.
    public void setObstacle(int x, int y, boolean isObstacle) {
        if (!inBounds(x, y)) return;
        obstacles[y][x] = isObstacle;
    }

    // checks if a cell is blocked
    public boolean isObstacle(int x, int y) {
        if (!inBounds(x, y)) return true; // treat out-of-bounds as blocked
        return obstacles[y][x];
    }
}
