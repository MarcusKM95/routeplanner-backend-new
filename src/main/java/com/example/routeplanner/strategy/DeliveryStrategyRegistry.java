package com.example.routeplanner.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Registry that holds all DeliveryStrategy beans and lets us look them up by name.
@Component
public class DeliveryStrategyRegistry {

    private final Map<String, DeliveryStrategy> strategies = new HashMap<>();
    private final DeliveryStrategy defaultStrategy;

    public DeliveryStrategyRegistry(List<DeliveryStrategy> strategies) {
        DeliveryStrategy foundDefault = null;

        for (DeliveryStrategy strategy : strategies) {
            String name = strategy.getName();
            this.strategies.put(name.toUpperCase(), strategy);
            if ("IN_ORDER".equalsIgnoreCase(name)) {
                foundDefault = strategy;
            }
        }

        // Fallback if IN_ORDER isn't present
        this.defaultStrategy = foundDefault != null
                ? foundDefault
                : (strategies.isEmpty() ? null : strategies.get(0));
    }

    public DeliveryStrategy getStrategy(String name) {
        if (name == null) {
            return defaultStrategy;
        }
        return strategies.getOrDefault(name.toUpperCase(), defaultStrategy);
    }
}
