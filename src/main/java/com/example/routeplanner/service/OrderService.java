package com.example.routeplanner.service;

import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.CreateOrderRequest;
import com.example.routeplanner.dto.OrderDTO;
import com.example.routeplanner.model.Order;
import com.example.routeplanner.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final CityMap cityMap;

    // In-memory storage for orders
    private final ConcurrentHashMap<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public OrderService(CityMap cityMap) {
        this.cityMap = cityMap;
    }

    public OrderDTO createOrder(CreateOrderRequest req) {
        if (req.restaurantId() == null || req.restaurantId().isBlank()) {
            throw new IllegalArgumentException("restaurantId is required");
        }

        // Validate restaurant exists
        var restaurantOpt = cityMap.findRestaurantById(req.restaurantId());
        if (restaurantOpt.isEmpty()) {
            throw new IllegalArgumentException("Unknown restaurant id: " + req.restaurantId());
        }

        // Validate coordinates inside grid
        var grid = cityMap.getGrid();
        if (!grid.inBounds(req.x(), req.y())) {
            throw new IllegalArgumentException(
                    "Order coordinates out of bounds: (" + req.x() + ", " + req.y() + ")"
            );
        }

        long id = idSequence.getAndIncrement();

        String label = (req.label() == null || req.label().isBlank())
                ? ("Order #" + id)
                : req.label();

        Order order = new Order(
                id,
                req.restaurantId(),
                req.x(),
                req.y(),
                label
        );

        orders.put(id, order);

        return toDTO(order);
    }

    public List<OrderDTO> listOrders() {
        return orders.values().stream()
                .sorted((o1, o2) -> Long.compare(o1.getId(), o2.getId()))
                .map(this::toDTO)
                .toList();
    }

    public OrderDTO getOrder(long id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + id);
        }
        return toDTO(order);
    }

    // Internal method to get the Order entity by ID
    public Order getOrderEntity(long id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + id);
        }
        return order;
    }

    // Add assignCourier and updateStatus here later

    private OrderDTO toDTO(Order order) {
        return new OrderDTO(
                order.getId(),
                order.getRestaurantId(),
                order.getX(),
                order.getY(),
                order.getLabel(),
                order.getStatus().name(),
                order.getAssignedCourierId()
        );
    }
}
