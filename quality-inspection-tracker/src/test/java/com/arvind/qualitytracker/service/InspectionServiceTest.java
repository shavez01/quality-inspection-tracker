package com.arvind.qualitytracker.service;

import com.arvind.qualitytracker.dto.CreateInspectionRequest;
import com.arvind.qualitytracker.dto.ResolveInspectionRequest;
import com.arvind.qualitytracker.exception.InspectionAlreadyResolvedException;
import com.arvind.qualitytracker.exception.InspectionNotFoundException;
import com.arvind.qualitytracker.model.DefectType;
import com.arvind.qualitytracker.model.Inspection;
import com.arvind.qualitytracker.model.InspectionStatus;
import com.arvind.qualitytracker.model.Severity;
import com.arvind.qualitytracker.repository.InspectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionRepository repository;

    private InspectionService service;

    @BeforeEach
    void setUp() {
        service = new InspectionService(repository);
    }

    @Test
    void createShouldTrimInput() {
        Inspection inspection = new Inspection(
                1L, LocalDate.of(2026, 8, 25), "LINE-04",
                DefectType.WEAVE_DEFECT, Severity.MAJOR, "Uneven weave",
                InspectionStatus.OPEN, null, LocalDateTime.now(), null
        );

        when(repository.save(any(), eq("LINE-04"), any(), any(), eq("Uneven weave")))
                .thenReturn(inspection);

        var response = service.create(new CreateInspectionRequest(
                LocalDate.of(2026, 8, 25), " LINE-04 ",
                DefectType.WEAVE_DEFECT, Severity.MAJOR, " Uneven weave "
        ));

        assertEquals("LINE-04", response.machineLineId());
        assertEquals("Uneven weave", response.remarks());
    }

    @Test
    void resolveShouldRejectMissingInspection() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                InspectionNotFoundException.class,
                () -> service.resolve(10L, new ResolveInspectionRequest("Fixed"))
        );
    }

    @Test
    void resolveShouldRejectAlreadyResolvedInspection() {
        Inspection inspection = new Inspection(
                10L, LocalDate.of(2026, 8, 25), "LINE-01",
                DefectType.HOLE_TEAR, Severity.CRITICAL, null,
                InspectionStatus.RESOLVED, "Already fixed",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(repository.findById(10L)).thenReturn(Optional.of(inspection));

        assertThrows(
                InspectionAlreadyResolvedException.class,
                () -> service.resolve(10L, new ResolveInspectionRequest("Another note"))
        );

        verify(repository, never()).resolve(anyLong(), anyString());
    }
}
