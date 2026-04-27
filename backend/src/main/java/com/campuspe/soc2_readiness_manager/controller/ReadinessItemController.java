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

@RestController
@RequestMapping("/api/readiness-items")
public class ReadinessItemController {

    private final ReadinessItemService readinessItemService;

    public ReadinessItemController(ReadinessItemService readinessItemService) {
        this.readinessItemService = readinessItemService;
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ReadinessItem>> getAll(Pageable pageable) {
        Page<ReadinessItem> page = readinessItemService.getAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadinessItem> getById(@PathVariable Long id) {
        ReadinessItem item = readinessItemService.getById(id);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/create")
    public ResponseEntity<ReadinessItem> create(@Valid @RequestBody ReadinessItemRequest request) {
        ReadinessItem created = readinessItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
