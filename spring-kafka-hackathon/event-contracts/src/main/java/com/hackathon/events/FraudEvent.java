package com.hackathon.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Published by Fraud Service on fraud-events topic.
 * Consumed by Order Service.
 */
public final class FraudEvent {

    public enum Type {
        FRAUD_APPROVED,
        FRAUD_REJECTED
    }

    @NotBlank
    private final String orderId;

    @NotNull
    private final Type type;

    private final String reason;

    private final Instant timestamp;

    @JsonCreator
    public FraudEvent(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("type") Type type,
            @JsonProperty("reason") String reason,
            @JsonProperty("timestamp") Instant timestamp) {
        this.orderId = orderId;
        this.type = type;
        this.reason = reason;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static FraudEvent approved(String orderId) {
        return new FraudEvent(orderId, Type.FRAUD_APPROVED, null, Instant.now());
    }

    public static FraudEvent rejected(String orderId, String reason) {
        return new FraudEvent(orderId, Type.FRAUD_REJECTED, reason, Instant.now());
    }

    public String getOrderId() {
        return orderId;
    }

    public Type getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
