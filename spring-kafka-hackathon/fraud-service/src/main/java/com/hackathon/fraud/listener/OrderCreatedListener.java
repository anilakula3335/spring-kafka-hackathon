package com.hackathon.fraud.listener;

import com.hackathon.events.FraudEvent;
import com.hackathon.events.OrderCreatedEvent;
import com.hackathon.fraud.service.FraudChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final FraudChecker fraudChecker;
    private final KafkaTemplate<String, FraudEvent> kafkaTemplate;
    private final String fraudEventsTopic;

    public OrderCreatedListener(FraudChecker fraudChecker,
                                KafkaTemplate<String, FraudEvent> kafkaTemplate,
                                @Value("${fraud.topics.fraud-events:fraud-events}") String fraudEventsTopic) {
        this.fraudChecker = fraudChecker;
        this.kafkaTemplate = kafkaTemplate;
        this.fraudEventsTopic = fraudEventsTopic;
    }

    @KafkaListener(
            topics = "${fraud.topics.order-events:order-events}",
            groupId = "${spring.kafka.consumer.group-id:fraud-service}"
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Processing OrderCreated for orderId={}, amount={}", event.getOrderId(), event.getAmount());

        boolean approved = fraudChecker.isApproved(event);
        FraudEvent response = approved
                ? FraudEvent.approved(event.getOrderId())
                : FraudEvent.rejected(event.getOrderId(), "Order amount exceeds fraud threshold");

        kafkaTemplate.send(fraudEventsTopic, event.getOrderId(), response);
        log.info("Published {} for order {}", response.getType(), event.getOrderId());
    }
}
