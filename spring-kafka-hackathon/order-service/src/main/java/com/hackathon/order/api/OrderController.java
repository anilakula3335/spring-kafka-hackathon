package com.hackathon.order.api;

import com.hackathon.order.service.OrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    @Operation(summary = "Create order", description = "Creates a new order with PENDING status and publishes OrderCreated event")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        OrderResponse response = orderApplicationService.createOrder(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Returns all orders with their current status; message 'Data not available' when no orders exist")
    public ResponseEntity<OrderListResponse> getAllOrders() {
        List<OrderResponse> orders = orderApplicationService.findAll().stream()
                .map(OrderResponse::from)
                .toList();
        if (orders.isEmpty()) {
            return ResponseEntity.ok(new OrderListResponse("Data not available", Collections.emptyList()));
        }
        return ResponseEntity.ok(new OrderListResponse("Data retrieved successfully", orders));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Returns the order or 'Data not available' when order does not exist")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        return orderApplicationService.findById(orderId)
                .<ResponseEntity<?>>map(order -> ResponseEntity.ok(OrderResponse.from(order)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Data not available")));
    }
}
