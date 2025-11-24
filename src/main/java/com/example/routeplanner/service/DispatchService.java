package com.example.routeplanner.service;

import com.example.routeplanner.service.CourierRouteService;
import com.example.routeplanner.dto.MultiStopRouteResponse;
import com.example.routeplanner.dto.CourierDTO;
import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.OrderAssignmentDTO;
import com.example.routeplanner.dto.OrderDTO;
import com.example.routeplanner.dto.RouteResponse;
import com.example.routeplanner.model.Courier;
import com.example.routeplanner.model.Order;
import com.example.routeplanner.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DispatchService {

    private final OrderService orderService;
    private final CourierService courierService;
    private final CityMap cityMap;
    private final RouteService routeService;
    private final CourierRouteService courierRouteService;

    public DispatchService(OrderService orderService,
                           CourierService courierService,
                           CityMap cityMap,
                           RouteService routeService, CourierRouteService courierRouteService) {
        this.orderService = orderService;
        this.courierService = courierService;
        this.cityMap = cityMap;
        this.routeService = routeService;
        this.courierRouteService = courierRouteService;
    }


    // Assign the given order to the best courier based on total route distance
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

        // Find the restaurant for this order
        var restaurantOpt = cityMap.findRestaurantById(order.getRestaurantId());
        if (restaurantOpt.isEmpty()) {
            throw new IllegalStateException("Unknown restaurant id: " + order.getRestaurantId());
        }
        CityMap.Restaurant restaurant = restaurantOpt.get();

        int restX = restaurant.x();
        int restY = restaurant.y();
        int destX = order.getX();
        int destY = order.getY();

        Courier bestCourier = null;
        double bestScore = Double.POSITIVE_INFINITY;

        final double penaltyPerOrder = 15.0;

        for (Courier courier : couriers) {

            // route from courier to restaurant
            RouteResponse toRestaurant = routeService.computeRouteOnGrid(
                    cityMap.getGrid(),
                    courier.getCurrentX(),
                    courier.getCurrentY(),
                    restX,
                    restY,
                    Heuristic.MANHATTAN.name()
            );

            if (!Double.isFinite(toRestaurant.totalDistance()) ||
                    toRestaurant.path().isEmpty()) {
                // This courier cannot reach the restaurant (blocked, etc.).
                continue;
            }

            // route from restaurant to customer
            RouteResponse toCustomer = routeService.computeRouteOnGrid(
                    cityMap.getGrid(),
                    restX,
                    restY,
                    destX,
                    destY,
                    Heuristic.MANHATTAN.name()
            );

            if (!Double.isFinite(toCustomer.totalDistance()) ||
                    toCustomer.path().isEmpty()) {
                // This courier cannot reach the customer from the restaurant.
                continue;
            }

            double baseDistance = toRestaurant.totalDistance() + toCustomer.totalDistance();

            // penalty for how many orders this courier already has
            int existingOrders = courier.getAssignedOrderIds().size();
            double score = baseDistance + penaltyPerOrder * existingOrders;

            if (score < bestScore) {
                bestScore = score;
                bestCourier = courier;
            }
        }

        if (bestCourier == null) {
            throw new IllegalStateException("No courier can reach this order (blocked by city layout)");
        }

        bestCourier.assignOrder(order.getId());
        order.setAssignedCourierId(bestCourier.getId());
        order.setStatus(OrderStatus.ASSIGNED);

// Recompute the full multi-stop route for this courier
        MultiStopRouteResponse finalRoute =
                courierRouteService.computeRouteForCourier(
                        bestCourier.getId(),
                        Heuristic.MANHATTAN.name(),   // use Manhattan heuristic
                        "NEAREST_NEIGHBOR"            // or whatever strategy name you registered
                );

// Convert route path into int[] steps for movement
        var steps = finalRoute.path().stream()
                .map(p -> new int[]{ p.x(), p.y() })
                .toList();

// Store route in the courier so /api/sim/step can move them
        bestCourier.setActiveRoute(steps);

        OrderDTO orderDTO = orderService.getOrder(order.getId());
        CourierDTO courierDTO = courierService.toDTOPublic(bestCourier);

        return new OrderAssignmentDTO(orderDTO, courierDTO);
    }
}
