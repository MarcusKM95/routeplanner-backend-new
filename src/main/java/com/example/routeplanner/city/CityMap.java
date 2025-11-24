package com.example.routeplanner.city;

import com.example.routeplanner.model.Grid;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Represents the city map with terrain and restaurants.
@Component
public class CityMap {

    public enum CellType {
        ROAD,
        BUILDING,
        PARK,
        RIVER
    }

    public static final int CITY_WIDTH = 30;
    public static final int CITY_HEIGHT = 20;

    private final Grid grid;
    private final List<Restaurant> restaurants;
    private final CellType[][] cellTypes;

    public CityMap() {
        this.grid = new Grid(CITY_WIDTH, CITY_HEIGHT);
        this.restaurants = new ArrayList<>();
        this.cellTypes = new CellType[CITY_HEIGHT][CITY_WIDTH];

        for (int y = 0; y < CITY_HEIGHT; y++) {
            for (int x = 0; x < CITY_WIDTH; x++) {
                cellTypes[y][x] = CellType.ROAD;
            }
        }

        setupTerrain();
        setupRestaurants();
    }

    // Set up obstacles and weights in the city grid.
    private void setupTerrain() {


        //horizontal river across the map
        int riverY = 8;
        for (int x = 0; x < CITY_WIDTH; x++) {
            grid.setObstacle(x, riverY, true);
            cellTypes[riverY][x] = CellType.RIVER;
        }

        // Two bridges across the river
        grid.setObstacle(7, riverY, false);
        cellTypes[riverY][7] = CellType.ROAD;

        grid.setObstacle(20, riverY, false);
        cellTypes[riverY][20] = CellType.ROAD;

        // Building blocks left
        addBuildingBlock(2, 5, 6, 8);
        addBuildingBlock(2, 10, 7, 15);

        //Building blocks right
        addBuildingBlock(18, 2, 24, 5);
        addBuildingBlock(18, 11, 24, 17);

        // Add parks (slower, but walkable)
        addParkBlock(10, 2, 14, 6);
        addParkBlock(10, 12, 14, 16);

        // Add Major roads
        for (int y = 0; y < CITY_HEIGHT; y++) {
            if (y != 8) {
                grid.setObstacle(5, y, false);
                grid.setWeight(5, y, 0.7);
                cellTypes[y][5] = CellType.ROAD;
            }
        }

        for (int x = 0; x < CITY_WIDTH; x++) {
            grid.setObstacle(x, 4, false);
            grid.setWeight(x, 4, 0.7);
            cellTypes[4][x] = CellType.ROAD;
        }

    }
    // Helpers to add a building block

    private void addBuildingBlock(int x1, int y1, int x2, int y2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                grid.setObstacle(x, y, true);
                cellTypes[y][x] = CellType.BUILDING;
            }
        }
    }

    private void addParkBlock(int x1, int y1, int x2, int y2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                grid.setWeight(x, y, 1.4);
                cellTypes[y][x] = CellType.PARK;
            }
        }
    }


    private void setupRestaurants() {

        restaurants.clear();

        restaurants.add(new Restaurant(
                "pizzaplanet", "Pizza Planet",
                5, 4
        ));

        restaurants.add(new Restaurant(
                "sushihouse", "Sushi House",
                15, 4
        ));

        restaurants.add(new Restaurant(
                "burgerworld", "Burger World",
                20, 10
        ));
    }



    // Get the city grid.

    public Grid getGrid() {
        return grid;
    }

    // Get an unmodifiable list of restaurants
    public List<Restaurant> getRestaurants() {
        return Collections.unmodifiableList(restaurants);
    }

    public CellType[][] getCellTypes() {
        return cellTypes;
    }

    // Find a restaurant by its ID (case-insensitive)
    public Optional<Restaurant> findRestaurantById(String id) {
        if (id == null) return Optional.empty();
        return restaurants.stream()
                .filter(r -> r.id().equalsIgnoreCase(id))
                .findFirst();
    }

    // Simple record to represent a restaurant.
    public record Restaurant(
            String id,
            String name,
            int x,
            int y
    ) {
    }

}
