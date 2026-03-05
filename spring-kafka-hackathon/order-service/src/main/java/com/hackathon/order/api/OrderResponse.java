package com.hackathon.order.api;

import com.hackathon.order.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Order response")
public record OrderResponse(

        @Schema(description = "Order ID")
        String id,

        @Schema(description = "Product ID")
        String productId,

        @Schema(description = "Quantity")
        Integer quantity,

        @Schema(description = "Amount")
        BigDecimal amount,

        @Schema(description = "Customer ID")
        String customerId,

        @Schema(description = "Order status")
        String status,

        @Schema(description = "Created at")
        Instant createdAt,

        @Schema(description = "Updated at")
        Instant updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getProductId(),
                order.getQuantity(),
                order.getAmount(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
