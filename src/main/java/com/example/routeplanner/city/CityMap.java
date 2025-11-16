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

        int riverX = 14;
        for (int y = 0; y < CITY_HEIGHT; y++) {
            grid.setObstacle(riverX, y, true);
            cellTypes[y][riverX] = CellType.RIVER;
        }

        for (int x = 2; x <= 6; x++) {
            for (int y = 3; y <= 7; y++) {
                grid.setObstacle(x, y, true);
                cellTypes[y][x] = CellType.BUILDING;
            }
        }

        for (int x = 18; x <= 24; x++) {
            for (int y = 10; y <= 15; y++) {
                grid.setObstacle(x, y, true);
                cellTypes[y][x] = CellType.BUILDING;
            }
        }

        // Make a park area with higher movement cost (e.g., grass paths)
        for (int x = 8; x <= 12; x++) {
            for (int y = 12; y <= 17; y++) {
                grid.setWeight(x, y, 1.5); // slightly more expensive than normal roads
                cellTypes[y][x] = CellType.PARK;
            }
        }

        for (int x = 0; x < CITY_WIDTH; x++) {
            grid.setWeight(x, 9, 0.8);  // horizontal main road
        }
        for (int y = 0; y < CITY_HEIGHT; y++) {
            grid.setWeight(5, y, 0.8);  // vertical main road
        }
    }

    // Set up restaurants at fixed locations.
    private void setupRestaurants() {
        // NOTE: Make sure these coordinates are not on obstacles.
        restaurants.add(new Restaurant("pizzaplanet", "Pizza Planet", 4, 8));
        restaurants.add(new Restaurant("sushihouse", "Sushi House", 20, 5));
        restaurants.add(new Restaurant("burgerworld", "Burger World", 10, 16));
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
