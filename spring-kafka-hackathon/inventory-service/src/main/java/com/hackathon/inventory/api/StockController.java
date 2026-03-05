package com.hackathon.inventory.api;

import com.hackathon.inventory.service.InventoryValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stock")
@Tag(name = "Stock", description = "Inventory stock (read-only). Main flow is event-driven via Kafka.")
public class StockController {

    private final InventoryValidator inventoryValidator;

    public StockController(InventoryValidator inventoryValidator) {
        this.inventoryValidator = inventoryValidator;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get stock for a product")
    public int getStock(@PathVariable String productId) {
        return inventoryValidator.getStock(productId);
    }
}
