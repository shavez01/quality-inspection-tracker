package com.arvind.qualitytracker.dto;

import com.arvind.qualitytracker.model.DefectType;
import com.arvind.qualitytracker.model.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request data used when creating a new inspection.
 */
public record CreateInspectionRequest(
        @NotNull(message = "Inspection date is required")
        LocalDate inspectionDate,

        @NotBlank(message = "Machine/line ID is required")
        @Size(max = 100, message = "Machine/line ID must be 100 characters or less")
        String machineLineId,

        @NotNull(message = "Defect type is required")
        DefectType defectType,

        @NotNull(message = "Severity is required")
        Severity severity,

        @Size(max = 500, message = "Remarks must be 500 characters or less")
        String remarks
) {
}
