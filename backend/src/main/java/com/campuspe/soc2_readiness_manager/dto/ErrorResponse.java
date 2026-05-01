package com.campuspe.soc2_readiness_manager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardised error response")
public class ErrorResponse {
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    @Schema(description = "Error type", example = "Bad Request")
    private String error;
    @Schema(description = "Detailed error message", example = "Validation failed")
    private String message;
    @Schema(description = "Timestamp of the error", example = "2026-05-01T15:30:00")
    private LocalDateTime timestamp;
    @Schema(description = "Additional error details (e.g., validation errors)", example = "[\"Title is required\", \"Due date is required\"]")
    private List<String> details;
}
