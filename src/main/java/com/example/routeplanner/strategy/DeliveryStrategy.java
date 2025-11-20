package com.example.routeplanner.strategy;
import com.example.routeplanner.dto.DeliveryStopDTO;

import java.util.List;

public interface DeliveryStrategy {
    //Name used in API
    String getName();

    //Order the delivery stops based on the strategy implemented by the class
    List<DeliveryStopDTO> orderStops(List<DeliveryStopDTO> stops, int startX, int startY);

}
