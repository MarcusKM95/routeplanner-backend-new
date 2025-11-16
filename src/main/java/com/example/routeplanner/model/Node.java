package com.example.routeplanner.model;

/**
 * Internal search node used by A*.
 *
 * Each Node represents a single cell in the grid during the search.
 * It stores:
 * - gCost: cost from start to this node
 * - hCost: heuristic cost to the goal
 * - parent: used to reconstruct the final path
 */
public class Node implements Comparable<Node> { //Comparable for PriorityQueue

    private final int x;
    private final int y;

    private double gCost = Double.POSITIVE_INFINITY; // treat it as infinity initially (unknown)
    private double hCost = 0.0;
    private Node parent = null;

    private boolean closed = false; // true = A* has processed this node

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // --- Getters ---
    public int getX() { return x; }
    public int getY() { return y; }

    public double getGCost() { return gCost; }
    public double getHCost() { return hCost; }
    public double getFCost() { return gCost + hCost; }

    public Node getParent() { return parent; }
    public boolean isClosed() { return closed; }

    // --- Setters ---
    public void setGCost(double gCost) { this.gCost = gCost; }
    public void setHCost(double hCost) { this.hCost = hCost; }
    public void setParent(Node parent) { this.parent = parent; }
    public void setClosed(boolean closed) { this.closed = closed; }

    /**
     * Needed for PriorityQueue<Node>.
     * A* expands the node with the lowest fCost first.
     */
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.getFCost(), other.getFCost());
    }
}
