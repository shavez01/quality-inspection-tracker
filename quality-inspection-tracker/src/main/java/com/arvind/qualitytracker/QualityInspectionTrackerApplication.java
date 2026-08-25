package com.arvind.qualitytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Entry point for the Quality Inspection Tracker application.
 */
@SpringBootApplication
public class QualityInspectionTrackerApplication {

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of("data"));
        SpringApplication.run(QualityInspectionTrackerApplication.class, args);
    }
}
