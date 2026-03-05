package com.hackathon.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:order-service}")
    private String applicationName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Event-driven Order Service - create and query orders. All inter-service communication via Kafka.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Hackathon")
                                .url("https://github.com/spring-kafka-hackathon")));
    }
}
