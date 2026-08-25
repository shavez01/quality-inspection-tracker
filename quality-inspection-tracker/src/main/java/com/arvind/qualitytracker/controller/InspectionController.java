package com.arvind.qualitytracker.controller;

import com.arvind.qualitytracker.dto.CreateInspectionRequest;
import com.arvind.qualitytracker.dto.InspectionResponse;
import com.arvind.qualitytracker.dto.ResolveInspectionRequest;
import com.arvind.qualitytracker.dto.SummaryResponse;
import com.arvind.qualitytracker.model.InspectionStatus;
import com.arvind.qualitytracker.model.Severity;
import com.arvind.qualitytracker.service.InspectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Exposes REST endpoints for creating, viewing and resolving inspections.
 */
@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    private final InspectionService service;

    public InspectionController(InspectionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InspectionResponse> create(
            @Valid @RequestBody CreateInspectionRequest request) {
        InspectionResponse response = service.create(request);

        return ResponseEntity.created(
                URI.create("/api/inspections/" + response.id())
        ).body(response);
    }

    @GetMapping
    public List<InspectionResponse> findAll(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) InspectionStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "inspectionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return service.findAll(severity, status, from, to, sortBy, sortDir);
    }

    @PatchMapping("/{id}/resolve")
    public InspectionResponse resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolveInspectionRequest request) {
        return service.resolve(id, request);
    }

    @GetMapping("/summary")
    public SummaryResponse summary() {
        return service.summary();
    }
}
