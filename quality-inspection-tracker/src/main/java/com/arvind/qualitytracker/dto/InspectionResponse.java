package com.arvind.qualitytracker.dto;

import com.arvind.qualitytracker.model.Inspection;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * API response representation of an inspection.
 */
public record InspectionResponse(
        Long id,
        LocalDate inspectionDate,
        String machineLineId,
        String defectType,
        String severity,
        String remarks,
        String status,
        String resolutionNote,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
    public static InspectionResponse from(Inspection inspection) {
        return new InspectionResponse(
                inspection.id(),
                inspection.inspectionDate(),
                inspection.machineLineId(),
                inspection.defectType().name(),
                inspection.severity().name(),
                inspection.remarks(),
                inspection.status().name(),
                inspection.resolutionNote(),
                inspection.createdAt(),
                inspection.resolvedAt()
        );
    }
}
