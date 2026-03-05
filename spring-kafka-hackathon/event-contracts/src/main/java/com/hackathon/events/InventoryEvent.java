package com.hackathon.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Published by Inventory Service on inventory-events topic.
 * Consumed by Order Service.
 */
public final class InventoryEvent {

    public enum Type {
        INVENTORY_APPROVED,
        INVENTORY_REJECTED
    }

    @NotBlank
    private final String orderId;

    @NotNull
    private final Type type;

    private final String message;

    private final Instant timestamp;

    @JsonCreator
    public InventoryEvent(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("type") Type type,
            @JsonProperty("message") String message,
            @JsonProperty("timestamp") Instant timestamp) {
        this.orderId = orderId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static InventoryEvent approved(String orderId) {
        return new InventoryEvent(orderId, Type.INVENTORY_APPROVED, null, Instant.now());
    }

    public static InventoryEvent rejected(String orderId, String message) {
        return new InventoryEvent(orderId, Type.INVENTORY_REJECTED, message, Instant.now());
    }

    public String getOrderId() {
        return orderId;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
