package com.example.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(description = "Error response structure")
public record ErrorResponse(
        @Schema(description = "Error timestamp", example = "2023-07-15T12:30:45Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Error type", example = "Bad Request")
        String error,

        @Schema(description = "Detailed error message", example = "Invalid currency format")
        String message,

        @Schema(description = "API endpoint path", example = "/transfers")
        String path
) {}
