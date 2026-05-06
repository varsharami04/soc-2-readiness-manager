package com.campuspe.soc2_readiness_manager.controller;

import com.campuspe.soc2_readiness_manager.dto.ReadinessItemRequest;
import com.campuspe.soc2_readiness_manager.entity.ReadinessItem;
import com.campuspe.soc2_readiness_manager.service.ReadinessItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/readiness-items")
@Tag(name = "Readiness Items", description = "Endpoints for managing SOC 2 readiness items")
public class ReadinessItemController {

    private final ReadinessItemService readinessItemService;

    public ReadinessItemController(ReadinessItemService readinessItemService) {
        this.readinessItemService = readinessItemService;
    }

    @Operation(summary = "Get all readiness items", description = "Retrieves a paginated list of all non-deleted readiness items")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/all")
    public ResponseEntity<Page<ReadinessItem>> getAll(Pageable pageable) {
        Page<ReadinessItem> page = readinessItemService.getAll(pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get readiness item by ID", description = "Retrieves a specific readiness item by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved item")
    @ApiResponse(responseCode = "404", description = "Readiness item not found")
    @GetMapping("/{id}")
    public ResponseEntity<ReadinessItem> getById(@PathVariable Long id) {
        ReadinessItem item = readinessItemService.getById(id);
        return ResponseEntity.ok(item);
    }

    @Operation(summary = "Create readiness item", description = "Creates a new readiness item")
    @ApiResponse(responseCode = "201", description = "Successfully created item")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping("/create")
    public ResponseEntity<ReadinessItem> create(@Valid @RequestBody ReadinessItemRequest request) {
        ReadinessItem created = readinessItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
