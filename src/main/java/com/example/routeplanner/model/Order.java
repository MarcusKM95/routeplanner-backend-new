package com.example.routeplanner.model;

public class Order {

    private final long id;
    private final String restaurantId;
    private final int x;
    private final int y;
    private final String label;     //"Customer 1", "Order 11"
    private OrderStatus status;
    private String assignedCourierId; // null if not assigned

    public Order(long id, String restaurantId, int x, int y, String label) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.x = x;
        this.y = y;
        this.label = label;
        this.status = OrderStatus.NEW;
        this.assignedCourierId = null;
    }

    public long getId() {
        return id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getLabel() {
        return label;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getAssignedCourierId() {
        return assignedCourierId;
    }

    public void setAssignedCourierId(String assignedCourierId) {
        this.assignedCourierId = assignedCourierId;
    }
}
