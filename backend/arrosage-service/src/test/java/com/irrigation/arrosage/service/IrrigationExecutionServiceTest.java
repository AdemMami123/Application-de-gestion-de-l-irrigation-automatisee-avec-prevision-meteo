package com.irrigation.arrosage.service;

import com.irrigation.arrosage.dto.JournalArrosageDTO;
import com.irrigation.arrosage.entity.Parcelle;
import com.irrigation.arrosage.entity.ProgrammeArrosage;
import com.irrigation.arrosage.entity.ProgrammeArrosage.StatutProgramme;
import com.irrigation.arrosage.repository.ProgrammeArrosageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IrrigationExecutionServiceTest {

    @Mock
    private ProgrammeArrosageRepository programmeRepository;

    @Mock
    private JournalArrosageService journalService;

    @InjectMocks
    private IrrigationExecutionService executionService;

    private Parcelle testParcelle;
    private ProgrammeArrosage testProgramme;
    private LocalDateTime executionTime;

    @BeforeEach
    void setUp() {
        executionTime = LocalDateTime.of(2025, 11, 18, 10, 0);

        testParcelle = new Parcelle();
        testParcelle.setId(1L);
        testParcelle.setNom("Parcelle Test");
        testParcelle.setSuperficie(5000.0);
        testParcelle.setCulture("Tomates");

        testProgramme = new ProgrammeArrosage();
        testProgramme.setId(1L);
        testProgramme.setParcelle(testParcelle);
        testProgramme.setDatePlanifiee(LocalDateTime.of(2025, 11, 18, 8, 0));
        testProgramme.setDuree(60);
        testProgramme.setVolumePrevu(25.0);
        testProgramme.setStatut(StatutProgramme.PLANIFIE);
    }

    @Test
    void testExecuteScheduledPrograms_NoProgramsToExecute() {
        // Given
        when(programmeRepository.findByDatePlanifieeBeforeAndStatut(executionTime, StatutProgramme.PLANIFIE))
                .thenReturn(Collections.emptyList());

        // When
        int result = executionService.executeScheduledPrograms(executionTime);

        // Then
        assertThat(result).isEqualTo(0);
        verify(programmeRepository).findByDatePlanifieeBeforeAndStatut(executionTime, StatutProgramme.PLANIFIE);
        verify(journalService, never()).createJournal(any());
    }

    @Test
    void testExecuteScheduledPrograms_SingleProgramSuccess() {
        // Given
        when(programmeRepository.findByDatePlanifieeBeforeAndStatut(executionTime, StatutProgramme.PLANIFIE))
                .thenReturn(Collections.singletonList(testProgramme));
        when(programmeRepository.findById(1L)).thenReturn(Optional.of(testProgramme));
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(testProgramme);
        when(journalService.createJournal(any(JournalArrosageDTO.class)))
                .thenReturn(new JournalArrosageDTO());

        // When
        int result = executionService.executeScheduledPrograms(executionTime);

        // Then
        assertThat(result).isEqualTo(1);

        // Verify status transitions: PLANIFIE -> EN_COURS -> TERMINE
        ArgumentCaptor<ProgrammeArrosage> programmeCaptor = ArgumentCaptor.forClass(ProgrammeArrosage.class);
        verify(programmeRepository, times(2)).save(programmeCaptor.capture());

        List<ProgrammeArrosage> savedPrograms = programmeCaptor.getAllValues();
        assertThat(savedPrograms.get(0).getStatut()).isEqualTo(StatutProgramme.EN_COURS);
        assertThat(savedPrograms.get(1).getStatut()).isEqualTo(StatutProgramme.TERMINE);

        // Verify journal entry was created
        ArgumentCaptor<JournalArrosageDTO> journalCaptor = ArgumentCaptor.forClass(JournalArrosageDTO.class);
        verify(journalService).createJournal(journalCaptor.capture());

        JournalArrosageDTO journal = journalCaptor.getValue();
        assertThat(journal.getProgrammeId()).isEqualTo(1L);
        assertThat(journal.getVolumeReel()).isBetween(22.5, 27.5); // ±10% of 25.0
        assertThat(journal.getRemarque()).isNotNull();
    }

    @Test
    void testExecuteScheduledPrograms_MultipleProgramsSuccess() {
        // Given
        ProgrammeArrosage programme2 = new ProgrammeArrosage();
        programme2.setId(2L);
        programme2.setParcelle(testParcelle);
        programme2.setDatePlanifiee(LocalDateTime.of(2025, 11, 18, 9, 0));
        programme2.setDuree(45);
        programme2.setVolumePrevu(20.0);
        programme2.setStatut(StatutProgramme.PLANIFIE);

        when(programmeRepository.findByDatePlanifieeBeforeAndStatut(executionTime, StatutProgramme.PLANIFIE))
                .thenReturn(Arrays.asList(testProgramme, programme2));
        when(programmeRepository.findById(1L)).thenReturn(Optional.of(testProgramme));
        when(programmeRepository.findById(2L)).thenReturn(Optional.of(programme2));
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(testProgramme);
        when(journalService.createJournal(any(JournalArrosageDTO.class)))
                .thenReturn(new JournalArrosageDTO());

        // When
        int result = executionService.executeScheduledPrograms(executionTime);

        // Then
        assertThat(result).isEqualTo(2);
        verify(journalService, times(2)).createJournal(any(JournalArrosageDTO.class));
    }

    @Test
    void testExecuteSingleProgram_AlreadyInProgress() {
        // Given - Programme already in EN_COURS status
        testProgramme.setStatut(StatutProgramme.EN_COURS);
        when(programmeRepository.findById(1L)).thenReturn(Optional.of(testProgramme));

        // When
        executionService.executeSingleProgram(testProgramme, executionTime);

        // Then - Should skip execution
        verify(programmeRepository, never()).save(any());
        verify(journalService, never()).createJournal(any());
    }

    @Test
    void testExecuteSingleProgram_VolumeVariance() {
        // Given
        when(programmeRepository.findById(1L)).thenReturn(Optional.of(testProgramme));
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(testProgramme);
        when(journalService.createJournal(any(JournalArrosageDTO.class)))
                .thenReturn(new JournalArrosageDTO());

        // When - Execute multiple times to test variance
        for (int i = 0; i < 10; i++) {
            testProgramme.setStatut(StatutProgramme.PLANIFIE); // Reset status
            executionService.executeSingleProgram(testProgramme, executionTime);
        }

        // Then - All volumes should be within ±10% of planned
        ArgumentCaptor<JournalArrosageDTO> journalCaptor = ArgumentCaptor.forClass(JournalArrosageDTO.class);
        verify(journalService, times(10)).createJournal(journalCaptor.capture());

        List<JournalArrosageDTO> journals = journalCaptor.getAllValues();
        for (JournalArrosageDTO journal : journals) {
            assertThat(journal.getVolumeReel()).isBetween(22.5, 27.5);
        }
    }

    @Test
    void testExecuteSingleProgram_ExecutionFailure() {
        // Given
        when(programmeRepository.findById(1L)).thenReturn(Optional.of(testProgramme));
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(testProgramme);
        when(journalService.createJournal(any(JournalArrosageDTO.class)))
                .thenThrow(new RuntimeException("Journal creation failed"));

        // When/Then - Should throw exception
        assertThrows(RuntimeException.class, () -> {
            executionService.executeSingleProgram(testProgramme, executionTime);
        });

        // Status should be reverted to PLANIFIE for retry
        ArgumentCaptor<ProgrammeArrosage> programmeCaptor = ArgumentCaptor.forClass(ProgrammeArrosage.class);
        verify(programmeRepository, times(2)).save(programmeCaptor.capture());

        List<ProgrammeArrosage> savedPrograms = programmeCaptor.getAllValues();
        assertThat(savedPrograms.get(0).getStatut()).isEqualTo(StatutProgramme.EN_COURS);
        assertThat(savedPrograms.get(1).getStatut()).isEqualTo(StatutProgramme.PLANIFIE);
    }

    @Test
    void testExecuteScheduledPrograms_PartialFailure() {
        // Given
        ProgrammeArrosage programme2 = new ProgrammeArrosage();
        programme2.setId(2L);
        programme2.setParcelle(testParcelle);
        programme2.setDatePlanifiee(LocalDateTime.of(2025, 11, 18, 9, 0));
        programme2.setDuree(45);
        programme2.setVolumePrevu(20.0);
        programme2.setStatut(StatutProgramme.PLANIFIE);

        when(programmeRepository.findByDatePlanifieeBeforeAndStatut(executionTime, StatutProgramme.PLANIFIE))
                .thenReturn(Arrays.asList(testProgramme, programme2));
        when(programmeRepository.findById(1L)).thenReturn(Optional.of(testProgramme));
        when(programmeRepository.findById(2L)).thenReturn(Optional.of(programme2));
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(testProgramme);

        // First program succeeds, second fails
        when(journalService.createJournal(any(JournalArrosageDTO.class)))
                .thenReturn(new JournalArrosageDTO())
                .thenThrow(new RuntimeException("Failed to create journal"));

        // When
        int result = executionService.executeScheduledPrograms(executionTime);

        // Then - First program should succeed, second should fail
        assertThat(result).isEqualTo(1);

        // Second programme should be marked as ANNULE
        verify(programmeRepository, atLeast(1)).save(argThat(p ->
                p.getId() != null && p.getId().equals(2L) && p.getStatut() == StatutProgramme.ANNULE
        ));
    }

    @Test
    void testArchiveCompletedPrograms_NoProgramsToArchive() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        when(programmeRepository.findByDatePlanifieeBeforeAndStatut(cutoffDate, StatutProgramme.TERMINE))
                .thenReturn(Collections.emptyList());

        // When
        int result = executionService.archiveCompletedPrograms(cutoffDate);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void testArchiveCompletedPrograms_WithOldPrograms() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        testProgramme.setStatut(StatutProgramme.TERMINE);
        testProgramme.setDatePlanifiee(LocalDateTime.now().minusDays(35));

        when(programmeRepository.findByDatePlanifieeBeforeAndStatut(cutoffDate, StatutProgramme.TERMINE))
                .thenReturn(Collections.singletonList(testProgramme));

        // When
        int result = executionService.archiveCompletedPrograms(cutoffDate);

        // Then
        assertThat(result).isEqualTo(1);
    }

    @Test
    void testExecuteSingleProgram_RemarqueGeneration() {
        // Given
        when(programmeRepository.findById(1L)).thenReturn(Optional.of(testProgramme));
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(testProgramme);
        when(journalService.createJournal(any(JournalArrosageDTO.class)))
                .thenReturn(new JournalArrosageDTO());

        // When
        executionService.executeSingleProgram(testProgramme, executionTime);

        // Then
        ArgumentCaptor<JournalArrosageDTO> journalCaptor = ArgumentCaptor.forClass(JournalArrosageDTO.class);
        verify(journalService).createJournal(journalCaptor.capture());

        JournalArrosageDTO journal = journalCaptor.getValue();
        assertThat(journal.getRemarque()).containsAnyOf(
                "succès",
                "conforme",
                "supérieur",
                "inférieur"
        );
    }
}
