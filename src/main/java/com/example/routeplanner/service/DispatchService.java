package com.example.routeplanner.service;

import com.example.routeplanner.dto.CourierDTO;
import com.example.routeplanner.dto.OrderAssignmentDTO;
import com.example.routeplanner.dto.OrderDTO;
import com.example.routeplanner.dto.MultiStopRouteResponse;
import com.example.routeplanner.dto.PointDTO;
import com.example.routeplanner.model.Courier;
import com.example.routeplanner.model.Order;
import com.example.routeplanner.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DispatchService {

    private final OrderService orderService;
    private final CourierService courierService;
    private final CourierRouteService courierRouteService;

    public DispatchService(OrderService orderService,
                           CourierService courierService,
                           CourierRouteService courierRouteService) {
        this.orderService = orderService;
        this.courierService = courierService;
        this.courierRouteService = courierRouteService;
    }


    // Assign the given order to the best courier based on Manhattan distance
    public OrderAssignmentDTO assignOrderToBestCourier(long orderId) {
        Order order = orderService.getOrderEntity(orderId);

        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalArgumentException(
                    "Order " + orderId + " is not NEW (current status: " + order.getStatus() + ")"
            );
        }

        List<Courier> couriers = courierService.listCourierEntities();
        if (couriers.isEmpty()) {
            throw new IllegalStateException("No couriers available for assignment");
        }

        Courier bestCourier = null;
        int bestDistance = Integer.MAX_VALUE;

        int destX = order.getX();
        int destY = order.getY();

        for (Courier courier : couriers) {
            int dist = manhattan(
                    courier.getCurrentX(), courier.getCurrentY(),
                    destX, destY
            );
            if (dist < bestDistance) {
                bestDistance = dist;
                bestCourier = courier;
            }
        }

        if (bestCourier == null) {
            throw new IllegalStateException("Could not find a suitable courier");
        }

        // Update domain model
        bestCourier.assignOrder(order.getId());
        order.setAssignedCourierId(bestCourier.getId());
        order.setStatus(OrderStatus.ASSIGNED);

        MultiStopRouteResponse route =
                courierRouteService.computeRouteForCourier(
                        bestCourier.getId(),
                        "MANHATTAN",          // heuristic
                        "IN_ORDER"            // delivery strategy
                );

        List<int[]> steps = route.path().stream()
                .map(p -> new int[]{p.x(), p.y()})
                .toList();

        bestCourier.setActiveRoute(steps);

        // Convert to DTOs for response
        OrderDTO orderDTO = orderService.getOrder(order.getId());
        CourierDTO courierDTO = courierService.toDTOPublic(bestCourier);

        return new OrderAssignmentDTO(orderDTO, courierDTO);

    }

    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}
