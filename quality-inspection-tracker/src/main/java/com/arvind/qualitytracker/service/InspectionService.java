package com.arvind.qualitytracker.service;

import com.arvind.qualitytracker.dto.CreateInspectionRequest;
import com.arvind.qualitytracker.dto.InspectionResponse;
import com.arvind.qualitytracker.dto.ResolveInspectionRequest;
import com.arvind.qualitytracker.dto.SummaryResponse;
import com.arvind.qualitytracker.exception.InspectionAlreadyResolvedException;
import com.arvind.qualitytracker.exception.InspectionNotFoundException;
import com.arvind.qualitytracker.model.InspectionStatus;
import com.arvind.qualitytracker.model.Severity;
import com.arvind.qualitytracker.repository.InspectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Contains the business rules for managing quality inspections.
 */
@Service
public class InspectionService {

    private final InspectionRepository repository;

    public InspectionService(InspectionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public InspectionResponse create(CreateInspectionRequest request) {
        String machineLineId = request.machineLineId().trim();
        String remarks = request.remarks() == null ? null : request.remarks().trim();

        return InspectionResponse.from(repository.save(
                request.inspectionDate(),
                machineLineId,
                request.defectType(),
                request.severity(),
                remarks
        ));
    }

    @Transactional(readOnly = true)
    public List<InspectionResponse> findAll(Severity severity, InspectionStatus status,
                                             LocalDate from, LocalDate to,
                                             String sortBy, String sortDirection) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("From date cannot be after To date");
        }

        return repository.findAll(severity, status, from, to, sortBy, sortDirection)
                .stream()
                .map(InspectionResponse::from)
                .toList();
    }

    @Transactional
    public InspectionResponse resolve(Long id, ResolveInspectionRequest request) {
        var inspection = repository.findById(id)
                .orElseThrow(() -> new InspectionNotFoundException(id));

        if (inspection.status() == InspectionStatus.RESOLVED) {
            throw new InspectionAlreadyResolvedException(id);
        }

        int updated = repository.resolve(id, request.resolutionNote().trim());

        if (updated == 0) {
            throw new InspectionAlreadyResolvedException(id);
        }

        return repository.findById(id)
                .map(InspectionResponse::from)
                .orElseThrow(() -> new InspectionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary() {
        return new SummaryResponse(
                summaryFor(Severity.CRITICAL),
                summaryFor(Severity.MAJOR),
                summaryFor(Severity.MINOR)
        );
    }

    private SummaryResponse.SeveritySummary summaryFor(Severity severity) {
        return new SummaryResponse.SeveritySummary(
                repository.countBy(severity, InspectionStatus.OPEN),
                repository.countBy(severity, InspectionStatus.RESOLVED)
        );
    }
}
