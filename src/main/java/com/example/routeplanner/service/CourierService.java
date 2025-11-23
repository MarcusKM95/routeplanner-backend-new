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
        couriers.put("c1", new Courier("c1", "Anna", 3, 4));
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

}
