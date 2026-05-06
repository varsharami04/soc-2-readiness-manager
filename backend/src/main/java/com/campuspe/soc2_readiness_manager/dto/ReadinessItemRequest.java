package com.campuspe.soc2_readiness_manager.dto;

import com.campuspe.soc2_readiness_manager.entity.ControlCategory;
import com.campuspe.soc2_readiness_manager.entity.PriorityLevel;
import com.campuspe.soc2_readiness_manager.entity.ReadinessStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a readiness item")
public class ReadinessItemRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    @Schema(description = "Title of the readiness item", example = "Encrypt S3 Buckets")
    private String title;

    @NotBlank(message = "Control reference is required")
    @Size(max = 50, message = "Control reference must not exceed 50 characters")
    @Schema(description = "Control reference code", example = "CC-001")
    private String controlReference;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Detailed description", example = "Ensure all production S3 buckets have default encryption enabled using AES-256")
    private String description;

    @NotNull(message = "Category is required")
    @Schema(description = "Control category", example = "SECURITY")
    private ControlCategory category;

    @NotNull(message = "Status is required")
    @Schema(description = "Current status", example = "IN_PROGRESS")
    private ReadinessStatus status;

    @NotNull(message = "Priority is required")
    @Schema(description = "Priority level", example = "HIGH")
    private PriorityLevel priority;

    @NotBlank(message = "Owner name is required")
    @Size(max = 100, message = "Owner name must not exceed 100 characters")
    @Schema(description = "Name of the owner", example = "Jane Doe")
    private String ownerName;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email must be a valid email address")
    @Size(max = 150, message = "Owner email must not exceed 150 characters")
    @Schema(description = "Email of the owner", example = "jane.doe@example.com")
    private String ownerEmail;

    @NotNull(message = "Readiness score is required")
    @Min(value = 0, message = "Readiness score must be at least 0")
    @Max(value = 100, message = "Readiness score must not exceed 100")
    @Schema(description = "Readiness score out of 100", example = "75")
    private Integer readinessScore;

    @NotNull(message = "Due date is required")
    @Schema(description = "Target completion date", example = "2026-06-01")
    private LocalDate dueDate;

    @Size(max = 2000, message = "Evidence details must not exceed 2000 characters")
    @Schema(description = "Details about evidence", example = "Screenshot of AWS console showing bucket encryption enabled")
    private String evidenceDetails;

    @Size(max = 2000, message = "Risk summary must not exceed 2000 characters")
    @Schema(description = "Summary of risk", example = "Data could be compromised if buckets are public and unencrypted")
    private String riskSummary;

    @Size(max = 4000, message = "AI summary must not exceed 4000 characters")
    @Schema(description = "AI-generated summary (usually empty on creation)", example = "")
    private String aiSummary;
}
