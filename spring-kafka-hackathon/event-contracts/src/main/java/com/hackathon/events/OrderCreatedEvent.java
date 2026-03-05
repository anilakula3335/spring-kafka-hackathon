package com.hackathon.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published by Order Service on order-events topic when an order is created.
 * Consumed by Inventory Service and Fraud Service.
 */
public final class OrderCreatedEvent {

    @NotBlank
    private final String orderId;

    @NotBlank
    private final String productId;

    @NotNull
    @Min(1)
    private final Integer quantity;

    @NotNull
    @Min(0)
    private final BigDecimal amount;

    private final String customerId;

    private final Instant timestamp;

    @JsonCreator
    public OrderCreatedEvent(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("productId") String productId,
            @JsonProperty("quantity") Integer quantity,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("customerId") String customerId,
            @JsonProperty("timestamp") Instant timestamp) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
        this.customerId = customerId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
