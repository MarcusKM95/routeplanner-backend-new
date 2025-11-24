package com.example.routeplanner.service;

import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.CourierDTO;
import com.example.routeplanner.model.Courier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CourierService {

    private final ConcurrentHashMap<String, Courier> couriers = new ConcurrentHashMap<>();

    public CourierService(CityMap cityMap) {

        //manually seed some couriers at good positions on the map
        couriers.put("c1", new Courier("c1", "Anna", 1, 4));
        couriers.put("c2", new Courier("c2", "Jamal", 10, 10));
        couriers.put("c3", new Courier("c3", "Sofie", 20, 6));
    }

    public List<CourierDTO> listCouriers() {
        return couriers.values().stream()
                .map(this::toDTO)
                .toList();
    }

    public Courier getCourier(String courierId) {
        return couriers.get(courierId);
    }

    private CourierDTO toDTO(Courier courier) {
        return new CourierDTO(
                courier.getId(),
                courier.getName(),
                courier.getCurrentX(),
                courier.getCurrentY(),
                List.copyOf(courier.getAssignedOrderIds())
        );
    }

    public CourierDTO toDTOPublic(Courier courier) {
        return toDTO(courier);
    }


    // For internal use: list courier entities
    public List<Courier> listCourierEntities() {
        return couriers.values().stream().toList();
    }

    // Move all couriers one step along their active route
    public void stepAllCouriers() {
        for (Courier courier : couriers.values()) {
            stepCourier(courier);
        }
    }

    private void stepCourier(Courier courier) {
        List<int[]> route = courier.getActiveRoute();
        if (route == null || route.isEmpty()) return;

        // Take the next tile
        int[] next = route.get(0);
        courier.setCurrentX(next[0]);
        courier.setCurrentY(next[1]);

        // Remove it from the route
        route.remove(0);

        // If courier arrived at the stop
        if (route.isEmpty()) {
            // ✔ TODO later: pick up order or deliver it.
            // For now do nothing — just stop moving.
        }
    }


}
