package com.hackathon.order.service;

import com.hackathon.order.api.CreateOrderRequest;
import com.hackathon.order.api.OrderResponse;
import com.hackathon.order.domain.Order;
import com.hackathon.order.domain.OrderRepository;

import static com.hackathon.order.domain.Order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderApplicationService(OrderRepository orderRepository,
                                  OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(String orderId, CreateOrderRequest request) {
        Order order = new Order(
                orderId,
                request.productId(),
                request.quantity(),
                request.amount(),
                request.customerId()
        );
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        orderEventPublisher.publishOrderCreated(order);
        return OrderResponse.from(order);
    }

    public Optional<Order> findById(String orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
