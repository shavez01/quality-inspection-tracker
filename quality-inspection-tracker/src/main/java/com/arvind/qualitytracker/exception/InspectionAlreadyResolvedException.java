package com.arvind.qualitytracker.exception;

public class InspectionAlreadyResolvedException extends RuntimeException {
    public InspectionAlreadyResolvedException(Long id) {
        super("Inspection is already resolved: " + id);
    }
}
