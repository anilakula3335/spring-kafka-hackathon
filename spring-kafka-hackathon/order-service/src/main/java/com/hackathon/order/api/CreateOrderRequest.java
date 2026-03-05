package com.hackathon.order.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to create a new order")
public record CreateOrderRequest(

        @NotBlank
        @Schema(description = "Product identifier", example = "PRODUCT-001", requiredMode = Schema.RequiredMode.REQUIRED)
        String productId,

        @NotNull
        @Min(1)
        @Schema(description = "Quantity", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer quantity,

        @NotNull
        @DecimalMin("0")
        @Schema(description = "Order amount", example = "15000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal amount,

        @Schema(description = "Optional customer identifier", example = "CUST-001")
        String customerId
) {
}
