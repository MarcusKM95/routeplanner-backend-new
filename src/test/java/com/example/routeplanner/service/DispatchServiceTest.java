package com.example.routeplanner.service;

import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.CourierDTO;
import com.example.routeplanner.dto.MultiStopRouteResponse;
import com.example.routeplanner.dto.OrderAssignmentDTO;
import com.example.routeplanner.dto.OrderDTO;
import com.example.routeplanner.dto.PointDTO;
import com.example.routeplanner.dto.RouteResponse;
import com.example.routeplanner.model.Courier;
import com.example.routeplanner.model.Order;
import com.example.routeplanner.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CourierService courierService;

    @Mock
    private CityMap cityMap;

    @Mock
    private RouteService routeService;

    @Mock
    private CourierRouteService courierRouteService;

    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService = new DispatchService(
                orderService,
                courierService,
                cityMap,
                routeService,
                courierRouteService
        );
    }

    @Test
    void assignOrderToBestCourier_picksCourierWithLowestScoreAndSetsRoute() {
        long orderId = 1L;

        // --- Order setup ---
        Order order = new Order(orderId, "r1", 20, 10, "Test order");
        order.setStatus(OrderStatus.NEW);

        when(orderService.getOrderEntity(orderId)).thenReturn(order);

        // Return DTO for the result (we don't care too much about details here)
        when(orderService.getOrder(orderId))
                .thenReturn(new OrderDTO(
                        orderId,
                        order.getRestaurantId(),
                        order.getX(),
                        order.getY(),
                        order.getLabel(),
                        OrderStatus.ASSIGNED.name(),
                        "c2"
                ));

        // --- Restaurant setup ---
        CityMap.Restaurant restaurant =
                new CityMap.Restaurant("r1", "Test Restaurant", 10, 10);
        when(cityMap.findRestaurantById("r1")).thenReturn(Optional.of(restaurant));

        // --- Couriers setup ---
        Courier c1 = new Courier("c1", "Far Away", 0, 0);
        Courier c2 = new Courier("c2", "Close By", 9, 10);

        when(courierService.listCourierEntities())
                .thenReturn(List.of(c1, c2));

        // Convert courier to DTO for the result
        when(courierService.toDTOPublic(any(Courier.class))).thenAnswer(invocation -> {
            Courier c = invocation.getArgument(0);
            return new CourierDTO(
                    c.getId(),
                    c.getName(),
                    c.getCurrentX(),
                    c.getCurrentY(),
                    List.copyOf(c.getAssignedOrderIds())
            );
        });

        // --- RouteService behaviour ---
        // Use a simple "fake" Manhattan implementation so algorithmic logic is testable.
        when(routeService.computeRouteOnGrid(
                any(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyString()
        )).thenAnswer(invocation -> {
            int sx = invocation.getArgument(1);
            int sy = invocation.getArgument(2);
            int ex = invocation.getArgument(3);
            int ey = invocation.getArgument(4);

            int dx = Math.abs(ex - sx);
            int dy = Math.abs(ey - sy);
            int steps = dx + dy;

            List<PointDTO> path = new ArrayList<>();
            int x = sx;
            int y = sy;
            path.add(new PointDTO(x, y));

            // move in x
            while (x != ex) {
                x += (ex > x) ? 1 : -1;
                path.add(new PointDTO(x, y));
            }
            // move in y
            while (y != ey) {
                y += (ey > y) ? 1 : -1;
                path.add(new PointDTO(x, y));
            }

            return new RouteResponse(path, steps, path.size(), 0L);
        });

        // --- CourierRouteService behaviour for final multi-stop route ---
        when(courierRouteService.computeRouteForCourier(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(
                new MultiStopRouteResponse(
                        List.of(new PointDTO(1, 1), new PointDTO(2, 2)),
                        2.0,
                        0,
                        0L
                )
        );

        // --- Call method under test ---
        OrderAssignmentDTO assignment = dispatchService.assignOrderToBestCourier(orderId);

        // --- Assertions ---
        // c2 is much closer to the restaurant -> should be selected
        assertEquals("c2", assignment.courier().id(), "Best courier should be c2");

        // Order should now be assigned to c2 and have status ASSIGNED
        assertEquals("c2", order.getAssignedCourierId());
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());

        // Courier should have the order id in its assigned list
        assertTrue(c2.getAssignedOrderIds().contains(orderId));

        // CourierRouteService should be called for the chosen courier
        verify(courierRouteService, times(1))
                .computeRouteForCourier(eq("c2"), anyString(), anyString());

        // Active route should be translated from MultiStopRouteResponse points
        List<int[]> activeRoute = c2.getActiveRoute();
        assertEquals(2, activeRoute.size());
        assertArrayEquals(new int[]{1, 1}, activeRoute.get(0));
        assertArrayEquals(new int[]{2, 2}, activeRoute.get(1));
    }

    @Test
    void assignOrderToBestCourier_throwsIfOrderIsNotNew() {
        long orderId = 2L;
        Order order = new Order(orderId, "r1", 5, 5, "Old order");
        order.setStatus(OrderStatus.DELIVERED);

        when(orderService.getOrderEntity(orderId)).thenReturn(order);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.assignOrderToBestCourier(orderId)
        );

        assertTrue(ex.getMessage().contains("not NEW"));
        verifyNoInteractions(courierService, cityMap, routeService, courierRouteService);
    }

    @Test
    void assignOrderToBestCourier_throwsWhenNoCourierCanReachOrder() {
        long orderId = 3L;
        Order order = new Order(orderId, "r1", 5, 5, "Blocked order");
        order.setStatus(OrderStatus.NEW);

        when(orderService.getOrderEntity(orderId)).thenReturn(order);

        CityMap.Restaurant restaurant =
                new CityMap.Restaurant("r1", "Blocked Restaurant", 10, 10);
        when(cityMap.findRestaurantById("r1")).thenReturn(Optional.of(restaurant));

        Courier c1 = new Courier("c1", "Blocked", 0, 0);
        when(courierService.listCourierEntities()).thenReturn(List.of(c1));

        // Any route calculation returns "no path" (infinite dist + empty path)
        when(routeService.computeRouteOnGrid(
                any(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyString()
        )).thenReturn(
                new RouteResponse(List.of(), Double.POSITIVE_INFINITY, 0, 0L)
        );

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dispatchService.assignOrderToBestCourier(orderId)
        );

        assertTrue(ex.getMessage().contains("No courier can reach"));
    }
}
