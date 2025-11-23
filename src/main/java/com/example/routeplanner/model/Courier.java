package com.example.routeplanner.model;

import java.util.ArrayList;
import java.util.List;

public class Courier {

    private final String id;
    private final String name;

    // Current position on the grid
    private int currentX;
    private int currentY;

    // IDs of assigned orders
    private final List<Long> assignedOrderIds = new ArrayList<>();

    public Courier(String id, String name, int currentX, int currentY) {
        this.id = id;
        this.name = name;
        this.currentX = currentX;
        this.currentY = currentY;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCurrentX() {
        return currentX;
    }

    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }

    public List<Long> getAssignedOrderIds() {
        return assignedOrderIds;
    }

    public void assignOrder(long orderId) {
        assignedOrderIds.add(orderId);
    }
}
