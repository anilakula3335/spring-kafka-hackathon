package com.hackathon.order.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "List of orders with optional message when no data")
public record OrderListResponse(
        @Schema(description = "Message when no orders exist, e.g. 'Data not available'")
        String message,
        @Schema(description = "List of orders")
        List<OrderResponse> orders
) {}
