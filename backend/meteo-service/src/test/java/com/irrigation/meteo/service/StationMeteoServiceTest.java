package com.irrigation.meteo.service;

import com.irrigation.meteo.dto.StationMeteoDTO;
import com.irrigation.meteo.entity.StationMeteo;
import com.irrigation.meteo.repository.StationMeteoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour StationMeteoService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service StationMeteo")
class StationMeteoServiceTest {

    @Mock
    private StationMeteoRepository stationMeteoRepository;

    @InjectMocks
    private StationMeteoService stationMeteoService;

    private StationMeteo stationMeteo;
    private StationMeteoDTO stationMeteoDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        stationMeteo = StationMeteo.builder()
                .id(1L)
                .nom("Station Test")
                .latitude(new BigDecimal("48.8566"))
                .longitude(new BigDecimal("2.3522"))
                .fournisseur("MeteoFrance")
                .build();

        stationMeteoDTO = StationMeteoDTO.builder()
                .id(1L)
                .nom("Station Test")
                .latitude(new BigDecimal("48.8566"))
                .longitude(new BigDecimal("2.3522"))
                .fournisseur("MeteoFrance")
                .build();
    }

    @Test
    @DisplayName("Créer une station météo - devrait réussir")
    void testCreateStationMeteo_Success() {
        // Given
        when(stationMeteoRepository.save(any(StationMeteo.class))).thenReturn(stationMeteo);

        // When
        StationMeteoDTO result = stationMeteoService.create(stationMeteoDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNom()).isEqualTo("Station Test");
        assertThat(result.getFournisseur()).isEqualTo("MeteoFrance");
        verify(stationMeteoRepository, times(1)).save(any(StationMeteo.class));
    }

    @Test
    @DisplayName("Récupérer toutes les stations - devrait retourner une liste")
    void testFindAllStations_Success() {
        // Given
        StationMeteo station2 = StationMeteo.builder()
                .id(2L)
                .nom("Station Test 2")
                .latitude(new BigDecimal("45.7640"))
                .longitude(new BigDecimal("4.8357"))
                .fournisseur("OpenWeatherMap")
                .build();

        when(stationMeteoRepository.findAll()).thenReturn(Arrays.asList(stationMeteo, station2));

        // When
        List<StationMeteoDTO> results = stationMeteoService.findAll();

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getNom()).isEqualTo("Station Test");
        assertThat(results.get(1).getNom()).isEqualTo("Station Test 2");
        verify(stationMeteoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Récupérer une station par ID - devrait réussir")
    void testFindById_Success() {
        // Given
        when(stationMeteoRepository.findById(1L)).thenReturn(Optional.of(stationMeteo));

        // When
        StationMeteoDTO result = stationMeteoService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNom()).isEqualTo("Station Test");
        verify(stationMeteoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Récupérer une station par ID inexistant - devrait lancer une exception")
    void testFindById_NotFound() {
        // Given
        when(stationMeteoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stationMeteoService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Station météo non trouvée avec l'ID: 999");
        verify(stationMeteoRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Mettre à jour une station - devrait réussir")
    void testUpdateStation_Success() {
        // Given
        StationMeteoDTO updateDTO = StationMeteoDTO.builder()
                .nom("Station Updated")
                .latitude(new BigDecimal("43.2965"))
                .longitude(new BigDecimal("5.3698"))
                .fournisseur("UpdatedProvider")
                .build();

        when(stationMeteoRepository.findById(1L)).thenReturn(Optional.of(stationMeteo));
        when(stationMeteoRepository.save(any(StationMeteo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        StationMeteoDTO result = stationMeteoService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Station Updated");
        assertThat(result.getFournisseur()).isEqualTo("UpdatedProvider");
        verify(stationMeteoRepository, times(1)).findById(1L);
        verify(stationMeteoRepository, times(1)).save(any(StationMeteo.class));
    }

    @Test
    @DisplayName("Mettre à jour une station inexistante - devrait lancer une exception")
    void testUpdateStation_NotFound() {
        // Given
        when(stationMeteoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stationMeteoService.update(999L, stationMeteoDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Station météo non trouvée avec l'ID: 999");
        verify(stationMeteoRepository, times(1)).findById(999L);
        verify(stationMeteoRepository, never()).save(any(StationMeteo.class));
    }

    @Test
    @DisplayName("Supprimer une station - devrait réussir")
    void testDeleteStation_Success() {
        // Given
        when(stationMeteoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(stationMeteoRepository).deleteById(1L);

        // When
        stationMeteoService.delete(1L);

        // Then
        verify(stationMeteoRepository, times(1)).existsById(1L);
        verify(stationMeteoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Supprimer une station inexistante - devrait lancer une exception")
    void testDeleteStation_NotFound() {
        // Given
        when(stationMeteoRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> stationMeteoService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Station météo non trouvée avec l'ID: 999");
        verify(stationMeteoRepository, times(1)).existsById(999L);
        verify(stationMeteoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Rechercher par fournisseur - devrait retourner les stations correspondantes")
    void testFindByFournisseur_Success() {
        // Given
        when(stationMeteoRepository.findByFournisseur("MeteoFrance")).thenReturn(Arrays.asList(stationMeteo));

        // When
        List<StationMeteoDTO> results = stationMeteoService.findByFournisseur("MeteoFrance");

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFournisseur()).isEqualTo("MeteoFrance");
        verify(stationMeteoRepository, times(1)).findByFournisseur("MeteoFrance");
    }
}
