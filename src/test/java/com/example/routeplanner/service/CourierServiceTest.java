package com.example.routeplanner.service;

import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.model.Courier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

    @Mock
    private CityMap cityMap;

    @Mock
    private OrderService orderService;

    private CourierService courierService;

    @BeforeEach
    void setUp() {
        // CityMap is only used in constructor to seed couriers in this class,
        // so we can safely pass a mock here.
        courierService = new CourierService(cityMap, orderService);
    }

    @Test
    void stepAllCouriers_movesAlongRouteAndMarksOrdersDeliveredWhenDone() {
        // Get one of the seeded couriers (c1, c2, or c3)
        Courier courier = courierService.getCourier("c1");
        assertNotNull(courier, "Courier c1 should exist from seeding");

        int startX = courier.getCurrentX();
        int startY = courier.getCurrentY();

        // Give the courier a simple route: two steps in the +x direction
        List<int[]> route = new ArrayList<>();
        route.add(new int[]{startX + 1, startY}); // first step
        route.add(new int[]{startX + 2, startY}); // second (final) step
        courier.setActiveRoute(route);

        // Give the courier one assigned order
        long orderId = 42L;
        courier.assignOrder(orderId);
        assertEquals(1, courier.getAssignedOrderIds().size(),
                "Courier should have one assigned order before stepping");

        // === First step ===
        courierService.stepAllCouriers();

        // Courier should have moved to the first point
        assertEquals(startX + 1, courier.getCurrentX());
        assertEquals(startY, courier.getCurrentY());

        // Route should now have one remaining step
        assertEquals(1, courier.getActiveRoute().size(),
                "One step should remain after first step");

        // No orders should be marked as delivered yet
        verify(orderService, never()).markOrdersDeliveredForCourier(anyString());
        assertEquals(1, courier.getAssignedOrderIds().size(),
                "Assigned orders should not be cleared after first step");

        // === Second step (finishing route) ===
        courierService.stepAllCouriers();

        // Courier should now be at the final location
        assertEquals(startX + 2, courier.getCurrentX());
        assertEquals(startY, courier.getCurrentY());

        // Route should be empty after finishing
        assertTrue(courier.getActiveRoute().isEmpty(),
                "Active route should be empty after reaching the final step");

        // Assigned orders should be cleared
        assertTrue(courier.getAssignedOrderIds().isEmpty(),
                "Assigned orders should be cleared after finishing route");

        // markOrdersDeliveredForCourier should be called once for this courier
        verify(orderService, times(1))
                .markOrdersDeliveredForCourier(eq(courier.getId()));
    }

    @Test
    void stepAllCouriers_doesNothingWhenRouteIsEmpty() {
        Courier courier = courierService.getCourier("c2");
        assertNotNull(courier);

        int startX = courier.getCurrentX();
        int startY = courier.getCurrentY();

        // Ensure route is empty
        courier.setActiveRoute(List.of());

        courierService.stepAllCouriers();

        // Position should not change
        assertEquals(startX, courier.getCurrentX());
        assertEquals(startY, courier.getCurrentY());

        // No interactions with orderService for this courier
        verify(orderService, never()).markOrdersDeliveredForCourier(anyString());
    }
}
