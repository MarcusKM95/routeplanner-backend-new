package com.example.routeplanner.service;

import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.DeliveryStopDTO;
import com.example.routeplanner.dto.MultiStopRouteResponse;
import com.example.routeplanner.dto.PointDTO;
import com.example.routeplanner.model.Courier;
import com.example.routeplanner.model.Order;
import com.example.routeplanner.strategy.DeliveryStrategy;
import com.example.routeplanner.strategy.DeliveryStrategyRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourierRouteService {

    private final CityMap cityMap;
    private final OrderService orderService;
    private final CourierService courierService;
    private final DeliveryStrategyRegistry deliveryStrategyRegistry;
    private final RouteService routeService;

    public CourierRouteService(CityMap cityMap,
                               OrderService orderService,
                               CourierService courierService,
                               DeliveryStrategyRegistry deliveryStrategyRegistry,
                               RouteService routeService) {
        this.cityMap = cityMap;
        this.orderService = orderService;
        this.courierService = courierService;
        this.deliveryStrategyRegistry = deliveryStrategyRegistry;
        this.routeService = routeService;
    }

    // Compute multi-stop route for the given courier using specified strategy and heuristic
    public MultiStopRouteResponse computeRouteForCourier(String courierId,
                                                         String heuristic,
                                                         String strategyName) {

        Courier courier = courierService.getCourier(courierId);
        if (courier == null) {
            throw new IllegalArgumentException("Unknown courier id: " + courierId);
        }

        List<Long> orderIds = courier.getAssignedOrderIds();
        if (orderIds.isEmpty()) {
            return new MultiStopRouteResponse(
                    List.of(),
                    0.0,
                    0,
                    0L
            );
        }

        // Load Order entities
        List<Order> orders = new ArrayList<>(orderIds.size());
        for (Long orderId : orderIds) {
            orders.add(orderService.getOrderEntity(orderId));
        }

        // Build stops: For each order, first restaurant pickup, then customer delivery
        List<DeliveryStopDTO> stops = new ArrayList<>(orders.size() * 2);

        for (Order order : orders) {
            var restaurantOpt = cityMap.findRestaurantById(order.getRestaurantId());
            if (restaurantOpt.isEmpty()) {
                throw new IllegalStateException("Restaurant not found for order: " + order.getId());
            }
            var restaurant = restaurantOpt.get();

            // 1) Pickup stop
            stops.add(new DeliveryStopDTO(
                    restaurant.x(),
                    restaurant.y(),
                    "Pickup " + order.getLabel() + " at " + restaurant.name()
            ));

            // 2) Delivery stop
            stops.add(new DeliveryStopDTO(
                    order.getX(),
                    order.getY(),
                    "Deliver " + order.getLabel()
            ));
        }

        // Choose strategy
        DeliveryStrategy strategy = deliveryStrategyRegistry.getStrategy(strategyName);

        int currentX = courier.getCurrentX();
        int currentY = courier.getCurrentY();

        List<DeliveryStopDTO> orderedStops =
                strategy.orderStops(stops, currentX, currentY);

        List<PointDTO> fullPath = new ArrayList<>();
        double totalDistance = 0.0;
        int totalVisitedNodes = 0;
        long totalTimeMs = 0L;

        boolean firstLeg = true;

        for (DeliveryStopDTO stop : orderedStops) {

            var leg = routeService.computeRouteOnGrid(
                    cityMap.getGrid(),     // your real grid access
                    currentX,
                    currentY,
                    stop.x(),
                    stop.y(),
                    heuristic
            );

            totalVisitedNodes += leg.visitedNodes();
            totalTimeMs += leg.timeMs();

            if (!Double.isFinite(leg.totalDistance()) || leg.path().isEmpty()) {
                totalDistance = Double.POSITIVE_INFINITY;
                break;
            }

            totalDistance += leg.totalDistance();

            if (firstLeg) {
                fullPath.addAll(leg.path());
                firstLeg = false;
            } else {
                for (int i = 1; i < leg.path().size(); i++) {
                    fullPath.add(leg.path().get(i));
                }
            }

            // Move start to this stop
            currentX = stop.x();
            currentY = stop.y();
        }

        return new MultiStopRouteResponse(
                fullPath,
                totalDistance,
                totalVisitedNodes,
                totalTimeMs
        );
    }
}
