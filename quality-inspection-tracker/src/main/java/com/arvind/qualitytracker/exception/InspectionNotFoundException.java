package com.arvind.qualitytracker.exception;

public class InspectionNotFoundException extends RuntimeException {
    public InspectionNotFoundException(Long id) {
        super("Inspection not found: " + id);
    }
}
