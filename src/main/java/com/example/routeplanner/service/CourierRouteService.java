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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        var orderIds = courier.getAssignedOrderIds();
        if (orderIds.isEmpty()) {
            return new MultiStopRouteResponse(List.of(), 0.0, 0, 0L);
        }

        // 1) Load Order entities
        List<Order> orders = new ArrayList<>(orderIds.size());
        for (Long id : orderIds) {
            orders.add(orderService.getOrderEntity(id));
        }

        // 2) Group orders by restaurantId (preserve insertion order)
        Map<String, List<Order>> byRestaurant = new LinkedHashMap<>();
        for (Order o : orders) {
            byRestaurant
                    .computeIfAbsent(o.getRestaurantId(), k -> new ArrayList<>())
                    .add(o);
        }

        // Helper class for our groups
        class RestaurantGroup {
            final CityMap.Restaurant restaurant;
            final List<Order> orders;

            RestaurantGroup(CityMap.Restaurant restaurant, List<Order> orders) {
                this.restaurant = restaurant;
                this.orders = orders;
            }
        }

        // 3) Build list of restaurant groups
        List<RestaurantGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<Order>> entry : byRestaurant.entrySet()) {
            var restOpt = cityMap.findRestaurantById(entry.getKey());
            if (restOpt.isEmpty()) {
                throw new IllegalStateException("Unknown restaurant id: " + entry.getKey());
            }
            groups.add(new RestaurantGroup(restOpt.get(), entry.getValue()));
        }

        // 4) Prepare route accumulation
        int currentX = courier.getCurrentX();
        int currentY = courier.getCurrentY();

        List<PointDTO> fullPath = new ArrayList<>();
        double totalDistance = 0.0;
        int totalVisitedNodes = 0;
        long totalTimeMs = 0L;
        boolean firstLeg = true;

        // Remaining restaurant groups to visit
        List<RestaurantGroup> remainingGroups = new ArrayList<>(groups);

        // 5) Visit each restaurant group
        while (!remainingGroups.isEmpty()) {

            // 5a) Choose next restaurant group according to strategy
            RestaurantGroup nextGroup;

            if ("NEAREST_NEIGHBOR".equalsIgnoreCase(strategyName)) {
                double bestDist = Double.POSITIVE_INFINITY;
                RestaurantGroup bestGroup = null;

                for (RestaurantGroup g : remainingGroups) {
                    var legToRest = routeService.computeRouteOnGrid(
                            cityMap.getGrid(),
                            currentX, currentY,
                            g.restaurant.x(), g.restaurant.y(),
                            heuristic
                    );

                    if (!Double.isFinite(legToRest.totalDistance()) || legToRest.path().isEmpty()) {
                        continue; // can't reach this restaurant
                    }

                    if (legToRest.totalDistance() < bestDist) {
                        bestDist = legToRest.totalDistance();
                        bestGroup = g;
                    }
                }

                if (bestGroup == null) {
                    // cannot reach remaining restaurants -> route fails
                    return new MultiStopRouteResponse(
                            fullPath,
                            Double.POSITIVE_INFINITY,
                            totalVisitedNodes,
                            totalTimeMs
                    );
                }

                nextGroup = bestGroup;

            } else {
                // IN_ORDER or unknown => take next in insertion order
                nextGroup = remainingGroups.get(0);
            }

            // 5b) Go from current position to that restaurant (pickup)
            if (currentX != nextGroup.restaurant.x() || currentY != nextGroup.restaurant.y()) {
                var legToRest = routeService.computeRouteOnGrid(
                        cityMap.getGrid(),
                        currentX, currentY,
                        nextGroup.restaurant.x(), nextGroup.restaurant.y(),
                        heuristic
                );

                totalVisitedNodes += legToRest.visitedNodes();
                totalTimeMs += legToRest.timeMs();

                if (!Double.isFinite(legToRest.totalDistance()) || legToRest.path().isEmpty()) {
                    return new MultiStopRouteResponse(
                            fullPath,
                            Double.POSITIVE_INFINITY,
                            totalVisitedNodes,
                            totalTimeMs
                    );
                }

                totalDistance += legToRest.totalDistance();

                if (firstLeg) {
                    fullPath.addAll(legToRest.path());
                    firstLeg = false;
                } else {
                    // avoid duplicating joint node
                    for (int i = 1; i < legToRest.path().size(); i++) {
                        fullPath.add(legToRest.path().get(i));
                    }
                }

                currentX = nextGroup.restaurant.x();
                currentY = nextGroup.restaurant.y();
            }

            // From this restaurant, deliver to ALL its customers
            List<Order> remainingOrdersForRest = new ArrayList<>(nextGroup.orders);

            while (!remainingOrdersForRest.isEmpty()) {
                Order nextOrder;

                if ("NEAREST_NEIGHBOR".equalsIgnoreCase(strategyName)) {
                    double bestDist = Double.POSITIVE_INFINITY;
                    Order bestOrder = null;

                    for (Order o : remainingOrdersForRest) {
                        var legToCustomer = routeService.computeRouteOnGrid(
                                cityMap.getGrid(),
                                currentX, currentY,
                                o.getX(), o.getY(),
                                heuristic
                        );

                        if (!Double.isFinite(legToCustomer.totalDistance()) || legToCustomer.path().isEmpty()) {
                            continue;
                        }

                        if (legToCustomer.totalDistance() < bestDist) {
                            bestDist = legToCustomer.totalDistance();
                            bestOrder = o;
                        }
                    }

                    if (bestOrder == null) {
                        // can't reach remaining customers of this restaurant
                        return new MultiStopRouteResponse(
                                fullPath,
                                Double.POSITIVE_INFINITY,
                                totalVisitedNodes,
                                totalTimeMs
                        );
                    }

                    nextOrder = bestOrder;

                } else {
                    // IN_ORDER: keep insertion order of orders
                    nextOrder = remainingOrdersForRest.get(0);
                }

                // Leg: current -> that customer
                var legToCustomer = routeService.computeRouteOnGrid(
                        cityMap.getGrid(),
                        currentX, currentY,
                        nextOrder.getX(), nextOrder.getY(),
                        heuristic
                );

                totalVisitedNodes += legToCustomer.visitedNodes();
                totalTimeMs += legToCustomer.timeMs();

                if (!Double.isFinite(legToCustomer.totalDistance()) || legToCustomer.path().isEmpty()) {
                    return new MultiStopRouteResponse(
                            fullPath,
                            Double.POSITIVE_INFINITY,
                            totalVisitedNodes,
                            totalTimeMs
                    );
                }

                totalDistance += legToCustomer.totalDistance();

                if (firstLeg) {
                    fullPath.addAll(legToCustomer.path());
                    firstLeg = false;
                } else {
                    for (int i = 1; i < legToCustomer.path().size(); i++) {
                        fullPath.add(legToCustomer.path().get(i));
                    }
                }

                currentX = nextOrder.getX();
                currentY = nextOrder.getY();
                remainingOrdersForRest.remove(nextOrder);
            }

            // Done with this restaurant group
            remainingGroups.remove(nextGroup);
        }

        return new MultiStopRouteResponse(
                fullPath,
                totalDistance,
                totalVisitedNodes,
                totalTimeMs
        );
    }
}
