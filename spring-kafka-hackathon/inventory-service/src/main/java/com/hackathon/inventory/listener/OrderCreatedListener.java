package com.hackathon.inventory.listener;

import com.hackathon.events.InventoryEvent;
import com.hackathon.events.OrderCreatedEvent;
import com.hackathon.inventory.service.InventoryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final InventoryValidator inventoryValidator;
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    private final String inventoryEventsTopic;

    public OrderCreatedListener(InventoryValidator inventoryValidator,
                                KafkaTemplate<String, InventoryEvent> kafkaTemplate,
                                @Value("${inventory.topics.inventory-events:inventory-events}") String inventoryEventsTopic) {
        this.inventoryValidator = inventoryValidator;
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryEventsTopic = inventoryEventsTopic;
    }

    @KafkaListener(
            topics = "${inventory.topics.order-events:order-events}",
            groupId = "${spring.kafka.consumer.group-id:inventory-service}"
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Processing OrderCreated for orderId={}, productId={}, quantity={}",
                event.getOrderId(), event.getProductId(), event.getQuantity());

        boolean approved = inventoryValidator.validateAndReserve(event);
        String rejectMessage = !inventoryValidator.isKnownProduct(event.getProductId())
                ? "Unknown product " + event.getProductId()
                : "Insufficient stock for product " + event.getProductId();
        InventoryEvent response = approved
                ? InventoryEvent.approved(event.getOrderId())
                : InventoryEvent.rejected(event.getOrderId(), rejectMessage);

        kafkaTemplate.send(inventoryEventsTopic, event.getOrderId(), response);
        log.info("Published {} for order {}", response.getType(), event.getOrderId());
    }
}
