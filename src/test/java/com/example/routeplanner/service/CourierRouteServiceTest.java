package com.example.routeplanner.service;

import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.MultiStopRouteResponse;
import com.example.routeplanner.dto.PointDTO;
import com.example.routeplanner.dto.RouteResponse;
import com.example.routeplanner.model.Courier;
import com.example.routeplanner.model.Order;
import com.example.routeplanner.model.OrderStatus;
import com.example.routeplanner.model.Grid;
import com.example.routeplanner.strategy.DeliveryStrategyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierRouteServiceTest {

    @Mock
    private CityMap cityMap;

    @Mock
    private OrderService orderService;

    @Mock
    private CourierService courierService;

    @Mock
    private DeliveryStrategyRegistry deliveryStrategyRegistry;

    @Mock
    private RouteService routeService;

    private CourierRouteService courierRouteService;

    @BeforeEach
    void setUp() {
        courierRouteService = new CourierRouteService(
                cityMap,
                orderService,
                courierService,
                deliveryStrategyRegistry,
                routeService
        );
    }

    @Test
    void computeRouteForCourier_noAssignedOrders_returnsEmptyRoute() {
        Courier courier = new Courier("c1", "Anna", 0, 0);
        // no assigned orders
        assertTrue(courier.getAssignedOrderIds().isEmpty());

        when(courierService.getCourier("c1")).thenReturn(courier);

        MultiStopRouteResponse res =
                courierRouteService.computeRouteForCourier("c1", "MANHATTAN", "IN_ORDER");

        assertTrue(res.path().isEmpty());
        assertEquals(0.0, res.totalDistance(), 1e-9);
        assertEquals(0, res.visitedNodes());
        assertEquals(0L, res.timeMs());
    }

    @Test
    void computeRouteForCourier_singleRestaurant_twoOrders_inOrderStrategy() {
        // --- Courier with two assigned orders ---
        Courier courier = new Courier("c1", "Anna", 0, 0);
        when(courierService.getCourier("c1")).thenReturn(courier);

        Order o1 = new Order(1L, "r1", 10, 0, "O1");
        o1.setStatus(OrderStatus.ASSIGNED);
        Order o2 = new Order(2L, "r1", 12, 0, "O2");
        o2.setStatus(OrderStatus.ASSIGNED);

        courier.assignOrder(o1.getId());
        courier.assignOrder(o2.getId());

        // order ids in insertion order
        assertEquals(List.of(1L, 2L), courier.getAssignedOrderIds());

        when(orderService.getOrderEntity(1L)).thenReturn(o1);
        when(orderService.getOrderEntity(2L)).thenReturn(o2);

        // --- Restaurant definition ---
        CityMap.Restaurant rest =
                new CityMap.Restaurant("r1", "R1", 5, 0);
        when(cityMap.findRestaurantById("r1"))
                .thenReturn(Optional.of(rest));

        // grid (not actually used by our fake RouteService)
        when(cityMap.getGrid()).thenReturn(new Grid(50, 50));

        // --- Fake route algorithm: simple Manhattan path like before ---
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

            while (x != ex) {
                x += (ex > x) ? 1 : -1;
                path.add(new PointDTO(x, y));
            }
            while (y != ey) {
                y += (ey > y) ? 1 : -1;
                path.add(new PointDTO(x, y));
            }

            return new RouteResponse(path, steps, path.size(), 0L);
        });

        // --- Call method under test ---
        MultiStopRouteResponse res =
                courierRouteService.computeRouteForCourier("c1", "MANHATTAN", "IN_ORDER");

        // We expect three legs:
        // (0,0) -> (5,0) : distance 5
        // (5,0) -> (10,0): distance 5
        // (10,0)-> (12,0): distance 2
        // total = 12
        assertEquals(12.0, res.totalDistance(), 1e-9);

        // Path should start at courier position and end at last customer
        assertFalse(res.path().isEmpty());
        assertEquals(new PointDTO(0, 0), res.path().get(0));
        assertEquals(new PointDTO(12, 0), res.path().get(res.path().size() - 1));

        // With our path construction and overlap-removal logic, total points should be 13
        assertEquals(13, res.path().size());
    }

    @Test
    void computeRouteForCourier_unknownCourier_throwsIllegalArgumentException() {
        when(courierService.getCourier("unknown")).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> courierRouteService.computeRouteForCourier("unknown", "MANHATTAN", "IN_ORDER")
        );
    }
}
//         List<CourierDTO> courierDTOs = dispatchService.getAllCouriersWithStatus();