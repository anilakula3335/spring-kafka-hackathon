package com.hackathon.order.service;

import com.hackathon.events.FraudEvent;
import com.hackathon.events.InventoryEvent;
import com.hackathon.order.domain.Order;
import com.hackathon.order.domain.OrderRepository;

import static com.hackathon.order.domain.Order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks inventory and fraud responses per order and updates final order status
 * when both responses have been received.
 */
@Component
public class OrderStatusAggregator {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusAggregator.class);

    private final OrderRepository orderRepository;

    /**
     * orderId -> (inventoryReceived, fraudReceived, inventoryApproved, fraudApproved)
     */
    private final Map<String, PendingResponses> pending = new ConcurrentHashMap<>();

    public OrderStatusAggregator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "${order.topics.inventory-events:inventory-events}",
            groupId = "${spring.kafka.consumer.group-id:order-service}",
            containerFactory = "inventoryListenerContainerFactory"
    )
    @Transactional
    public void onInventoryEvent(InventoryEvent event) {
        log.info("Received inventory event for order {}: {}", event.getOrderId(), event.getType());
        pending.compute(event.getOrderId(), (id, p) -> {
            PendingResponses current = p != null ? p : new PendingResponses();
            current.inventoryReceived = true;
            current.inventoryApproved = event.getType() == InventoryEvent.Type.INVENTORY_APPROVED;
            return current;
        });
        tryFinalizeOrder(event.getOrderId());
    }

    @KafkaListener(
            topics = "${order.topics.fraud-events:fraud-events}",
            groupId = "${spring.kafka.consumer.group-id:order-service}",
            containerFactory = "fraudListenerContainerFactory"
    )
    @Transactional
    public void onFraudEvent(FraudEvent event) {
        log.info("Received fraud event for order {}: {}", event.getOrderId(), event.getType());
        pending.compute(event.getOrderId(), (id, p) -> {
            PendingResponses current = p != null ? p : new PendingResponses();
            current.fraudReceived = true;
            current.fraudApproved = event.getType() == FraudEvent.Type.FRAUD_APPROVED;
            return current;
        });
        tryFinalizeOrder(event.getOrderId());
    }

    private void tryFinalizeOrder(String orderId) {
        PendingResponses p = pending.get(orderId);
        if (p == null || !p.inventoryReceived || !p.fraudReceived) {
            return;
        }
        pending.remove(orderId);
        orderRepository.findById(orderId).ifPresent(order -> {
            OrderStatus newStatus = (p.inventoryApproved && p.fraudApproved)
                    ? OrderStatus.CONFIRMED
                    : OrderStatus.REJECTED;
            order.setStatus(newStatus);
            orderRepository.save(order);
            log.info("Order {} finalized with status {}", orderId, newStatus);
        });
    }

    private static class PendingResponses {
        boolean inventoryReceived;
        boolean fraudReceived;
        boolean inventoryApproved;
        boolean fraudApproved;
    }
}
