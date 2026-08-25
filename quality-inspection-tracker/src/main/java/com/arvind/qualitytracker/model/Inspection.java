package com.arvind.qualitytracker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a quality inspection recorded for a machine or production line.
 */
public record Inspection(
        Long id,
        LocalDate inspectionDate,
        String machineLineId,
        DefectType defectType,
        Severity severity,
        String remarks,
        InspectionStatus status,
        String resolutionNote,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
