package com.hackathon.fraud.api;

import com.hackathon.fraud.service.FraudChecker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/fraud")
@Tag(name = "Fraud", description = "Fraud check config. Main flow is event-driven via Kafka.")
public class FraudController {

    private final FraudChecker fraudChecker;

    public FraudController(FraudChecker fraudChecker) {
        this.fraudChecker = fraudChecker;
    }

    @GetMapping("/threshold")
    @Operation(summary = "Get fraud threshold amount", description = "Orders with amount > threshold are rejected")
    public Map<String, BigDecimal> getThreshold() {
        return Map.of("amount", fraudChecker.getThresholdAmount());
    }
}
