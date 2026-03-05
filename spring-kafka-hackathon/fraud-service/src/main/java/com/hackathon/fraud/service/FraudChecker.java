package com.hackathon.fraud.service;

import com.hackathon.events.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * If order amount > threshold (default 50000) -> FraudRejected, else FraudApproved.
 */
@Service
public class FraudChecker {

    @Value("${fraud.threshold.amount:50000}")
    private BigDecimal thresholdAmount;

    public boolean isApproved(OrderCreatedEvent event) {
        return event.getAmount().compareTo(thresholdAmount) <= 0;
    }

    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }
}
