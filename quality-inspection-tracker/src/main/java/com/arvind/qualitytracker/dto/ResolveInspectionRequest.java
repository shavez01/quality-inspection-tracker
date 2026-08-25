package com.arvind.qualitytracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request data required to resolve an open inspection.
 */
public record ResolveInspectionRequest(
        @NotBlank(message = "Resolution note is required")
        @Size(max = 500, message = "Resolution note must be 500 characters or less")
        String resolutionNote
) {
}
