package com.irrigation.arrosage.scheduler;

import com.irrigation.arrosage.service.IrrigationExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IrrigationSchedulerTest {

    @Mock
    private IrrigationExecutionService executionService;

    @InjectMocks
    private IrrigationScheduler scheduler;

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    void testExecuteScheduledIrrigationPrograms_Success() {
        // Given
        when(executionService.executeScheduledPrograms(any(LocalDateTime.class))).thenReturn(3);

        // When
        scheduler.executeScheduledIrrigationPrograms();

        // Then
        verify(executionService, times(1)).executeScheduledPrograms(any(LocalDateTime.class));
    }

    @Test
    void testExecuteScheduledIrrigationPrograms_NoPrograms() {
        // Given
        when(executionService.executeScheduledPrograms(any(LocalDateTime.class))).thenReturn(0);

        // When
        scheduler.executeScheduledIrrigationPrograms();

        // Then
        verify(executionService, times(1)).executeScheduledPrograms(any(LocalDateTime.class));
    }

    @Test
    void testExecuteScheduledIrrigationPrograms_ExceptionHandling() {
        // Given
        when(executionService.executeScheduledPrograms(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Execution error"));

        // When - Should not throw exception (caught internally)
        scheduler.executeScheduledIrrigationPrograms();

        // Then
        verify(executionService, times(1)).executeScheduledPrograms(any(LocalDateTime.class));
    }

    @Test
    void testCleanupOldPrograms_Success() {
        // Given
        when(executionService.archiveCompletedPrograms(any(LocalDateTime.class))).thenReturn(5);

        // When
        scheduler.cleanupOldPrograms();

        // Then
        verify(executionService, times(1)).archiveCompletedPrograms(any(LocalDateTime.class));
    }

    @Test
    void testCleanupOldPrograms_NoPrograms() {
        // Given
        when(executionService.archiveCompletedPrograms(any(LocalDateTime.class))).thenReturn(0);

        // When
        scheduler.cleanupOldPrograms();

        // Then
        verify(executionService, times(1)).archiveCompletedPrograms(any(LocalDateTime.class));
    }

    @Test
    void testCleanupOldPrograms_ExceptionHandling() {
        // Given
        when(executionService.archiveCompletedPrograms(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Cleanup error"));

        // When - Should not throw exception
        scheduler.cleanupOldPrograms();

        // Then
        verify(executionService, times(1)).archiveCompletedPrograms(any(LocalDateTime.class));
    }

    @Test
    void testSchedulerContinuesAfterException() {
        // Given
        when(executionService.executeScheduledPrograms(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("First error"))
                .thenReturn(2);

        // When - Call twice to simulate scheduler continuing
        scheduler.executeScheduledIrrigationPrograms();
        scheduler.executeScheduledIrrigationPrograms();

        // Then - Should have been called twice
        verify(executionService, times(2)).executeScheduledPrograms(any(LocalDateTime.class));
    }
}
