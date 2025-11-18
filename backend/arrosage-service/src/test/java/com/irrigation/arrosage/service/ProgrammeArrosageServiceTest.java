package com.irrigation.arrosage.service;

import com.irrigation.arrosage.client.MeteoServiceClient;
import com.irrigation.arrosage.dto.PrevisionMeteoDTO;
import com.irrigation.arrosage.dto.ProgrammeArrosageDTO;
import com.irrigation.arrosage.entity.Parcelle;
import com.irrigation.arrosage.entity.ProgrammeArrosage;
import com.irrigation.arrosage.entity.ProgrammeArrosage.StatutProgramme;
import com.irrigation.arrosage.exception.ResourceNotFoundException;
import com.irrigation.arrosage.repository.ParcelleRepository;
import com.irrigation.arrosage.repository.ProgrammeArrosageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgrammeArrosageServiceTest {

    @Mock
    private ProgrammeArrosageRepository programmeRepository;

    @Mock
    private ParcelleRepository parcelleRepository;

    @Mock
    private MeteoServiceClient meteoServiceClient;

    @InjectMocks
    private ProgrammeArrosageService programmeService;

    private Parcelle parcelle;
    private ProgrammeArrosage programme;
    private ProgrammeArrosageDTO programmeDTO;
    private PrevisionMeteoDTO previsionDTO;

    @BeforeEach
    void setUp() {
        parcelle = new Parcelle();
        parcelle.setId(1L);
        parcelle.setNom("Parcelle Test");
        parcelle.setSuperficie(5000.0);
        parcelle.setCulture("Tomates");

        programme = new ProgrammeArrosage();
        programme.setId(1L);
        programme.setParcelle(parcelle);
        programme.setDatePlanifiee(LocalDateTime.now().plusDays(1));
        programme.setDuree(60);
        programme.setVolumePrevu(25.0);
        programme.setStatut(StatutProgramme.PLANIFIE);

        programmeDTO = new ProgrammeArrosageDTO();
        programmeDTO.setParcelleId(1L);
        programmeDTO.setDatePlanifiee(LocalDateTime.now().plusDays(1));
        programmeDTO.setDuree(60);
        programmeDTO.setVolumePrevu(25.0);
        programmeDTO.setStatut(StatutProgramme.PLANIFIE);

        previsionDTO = new PrevisionMeteoDTO();
        previsionDTO.setDate(LocalDateTime.now().plusDays(1));
        previsionDTO.setTemps("Ensoleillé");
        previsionDTO.setProbabilitePluie(10.0);
        previsionDTO.setVitesseVent(5.0);
    }

    @Test
    void testCreateProgramme() {
        when(parcelleRepository.findById(1L)).thenReturn(Optional.of(parcelle));
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(programme);

        ProgrammeArrosageDTO result = programmeService.createProgramme(programmeDTO);

        assertNotNull(result);
        assertEquals(StatutProgramme.PLANIFIE, result.getStatut());
        verify(parcelleRepository, times(1)).findById(1L);
        verify(programmeRepository, times(1)).save(any(ProgrammeArrosage.class));
    }

    @Test
    void testGetProgrammeById_Success() {
        when(programmeRepository.findById(1L)).thenReturn(Optional.of(programme));

        ProgrammeArrosageDTO result = programmeService.getProgrammeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getParcelleId());
        verify(programmeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProgrammeById_NotFound() {
        when(programmeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            programmeService.getProgrammeById(1L);
        });
    }

    @Test
    void testScheduleIrrigationBasedOnWeather_LowRainProbability() {
        when(parcelleRepository.findById(1L)).thenReturn(Optional.of(parcelle));
        when(meteoServiceClient.getPrevisionByStationAndDate(anyLong(), any()))
                .thenReturn(previsionDTO);
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(programme);

        LocalDateTime datePlanifiee = LocalDateTime.now().plusDays(1);
        ProgrammeArrosageDTO result = programmeService.scheduleIrrigationBasedOnWeather(1L, 1L, datePlanifiee);

        assertNotNull(result);
        verify(meteoServiceClient, times(1)).getPrevisionByStationAndDate(anyLong(), any());
        verify(programmeRepository, times(1)).save(any(ProgrammeArrosage.class));
    }

    @Test
    void testScheduleIrrigationBasedOnWeather_HighRainProbability() {
        previsionDTO.setProbabilitePluie(60.0);
        when(parcelleRepository.findById(1L)).thenReturn(Optional.of(parcelle));
        when(meteoServiceClient.getPrevisionByStationAndDate(anyLong(), any()))
                .thenReturn(previsionDTO);
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(programme);

        LocalDateTime datePlanifiee = LocalDateTime.now().plusDays(1);
        ProgrammeArrosageDTO result = programmeService.scheduleIrrigationBasedOnWeather(1L, 1L, datePlanifiee);

        assertNotNull(result);
        // Volume should be reduced due to high rain probability
        verify(programmeRepository, times(1)).save(any(ProgrammeArrosage.class));
    }

    @Test
    void testScheduleIrrigationBasedOnWeather_CircuitBreakerFallback() {
        when(parcelleRepository.findById(1L)).thenReturn(Optional.of(parcelle));
        when(meteoServiceClient.getPrevisionByStationAndDate(anyLong(), any()))
                .thenThrow(new RuntimeException("Service unavailable"));

        LocalDateTime datePlanifiee = LocalDateTime.now().plusDays(1);

        // The circuit breaker should handle the exception and use fallback logic
        assertDoesNotThrow(() -> {
            programmeService.scheduleIrrigationBasedOnWeather(1L, 1L, datePlanifiee);
        });
    }

    @Test
    void testGetProgrammesByParcelle() {
        List<ProgrammeArrosage> programmes = Arrays.asList(programme);
        when(programmeRepository.findByParcelleId(1L)).thenReturn(programmes);

        List<ProgrammeArrosageDTO> results = programmeService.getProgrammesByParcelle(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(programmeRepository, times(1)).findByParcelleId(1L);
    }

    @Test
    void testUpdateProgramme() {
        when(programmeRepository.findById(1L)).thenReturn(Optional.of(programme));
        when(parcelleRepository.findById(1L)).thenReturn(Optional.of(parcelle));
        when(programmeRepository.save(any(ProgrammeArrosage.class))).thenReturn(programme);

        programmeDTO.setDuree(90);
        ProgrammeArrosageDTO result = programmeService.updateProgramme(1L, programmeDTO);

        assertNotNull(result);
        verify(programmeRepository, times(1)).save(any(ProgrammeArrosage.class));
    }

    @Test
    void testDeleteProgramme() {
        when(programmeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(programmeRepository).deleteById(1L);

        programmeService.deleteProgramme(1L);

        verify(programmeRepository, times(1)).existsById(1L);
        verify(programmeRepository, times(1)).deleteById(1L);
    }
}
