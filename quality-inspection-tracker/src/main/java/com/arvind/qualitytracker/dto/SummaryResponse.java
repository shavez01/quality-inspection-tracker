package com.arvind.qualitytracker.dto;

/**
 * Provides the inspection count summary grouped by severity and status.
 */
public record SummaryResponse(
        SeveritySummary critical,
        SeveritySummary major,
        SeveritySummary minor
) {
    public record SeveritySummary(long open, long resolved) {
    }
}
