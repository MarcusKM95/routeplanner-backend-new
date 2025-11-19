package com.example.routeplanner.controller;

import com.example.routeplanner.city.CityMap;
import com.example.routeplanner.dto.CityCellDTO;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/city")
@CrossOrigin(origins = "*")
public class CityController {

    private final CityMap cityMap;

    public CityController(CityMap cityMap) {
        this.cityMap = cityMap;
    }

    // Returns the city layout as a flat list of cells.

    @GetMapping("/layout")
    public List<CityCellDTO> getCityLayout() {
        var cellTypes = cityMap.getCellTypes();
        int height = cityTypesHeight(cellTypes);
        int width = cityTypesWidth(cellTypes);

        List<CityCellDTO> cells = new ArrayList<>(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                var type = cellTypes[y][x];
                String typeName = type != null ? type.name() : "ROAD";
                cells.add(new CityCellDTO(x, y, typeName));
            }
        }

        return cells;
    }

    // helpers
    private int cityTypesHeight(CityMap.CellType[][] cellTypes) {
        return cellTypes.length;
    }

    private int cityTypesWidth(CityMap.CellType[][] cellTypes) {
        return cellTypes.length > 0 ? cellTypes[0].length : 0;
    }
}
