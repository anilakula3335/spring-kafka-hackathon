package com.hackathon.order.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Simple message response")
public record MessageResponse(
        @Schema(description = "Message text")
        String message
) {}
