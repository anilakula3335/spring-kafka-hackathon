package com.hackathon.inventory.service;

import com.hackathon.events.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates stock availability. Uses in-memory store initialized from config.
 */
@Service
public class InventoryValidator {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    @Value("${inventory.default-stock.PRODUCT-001:100}")
    private int product001Stock;
    @Value("${inventory.default-stock.PRODUCT-002:50}")
    private int product002Stock;
    @Value("${inventory.default-stock.PRODUCT-003:200}")
    private int product003Stock;

    @PostConstruct
    public void init() {
        stock.put("PRODUCT-001", product001Stock);
        stock.put("PRODUCT-002", product002Stock);
        stock.put("PRODUCT-003", product003Stock);
    }

    /**
     * Checks if there is enough stock. If yes, reserves (decrements) and returns true.
     * Unknown products (not in config) are treated as 0 stock and rejected.
     */
    public boolean validateAndReserve(OrderCreatedEvent event) {
        String productId = event.getProductId();
        int requested = event.getQuantity();
        if (!stock.containsKey(productId)) {
            return false; // unknown product → rejected (order will not stay PENDING)
        }
        int current = stock.get(productId);
        if (current < requested) {
            return false;
        }
        stock.merge(productId, requested, (old, req) -> old - req);
        return true;
    }

    /**
     * Returns current stock for a product (for API display). Unknown products return 0.
     */
    public int getStock(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    /**
     * Returns true if the product is in the catalog (has configured stock).
     */
    public boolean isKnownProduct(String productId) {
        return stock.containsKey(productId);
    }
}
