package com.irrigation.arrosage.service;

import com.irrigation.arrosage.dto.ParcelleDTO;
import com.irrigation.arrosage.entity.Parcelle;
import com.irrigation.arrosage.exception.ResourceNotFoundException;
import com.irrigation.arrosage.repository.ParcelleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelleServiceTest {

    @Mock
    private ParcelleRepository parcelleRepository;

    @InjectMocks
    private ParcelleService parcelleService;

    private Parcelle parcelle;
    private ParcelleDTO parcelleDTO;

    @BeforeEach
    void setUp() {
        parcelle = new Parcelle();
        parcelle.setId(1L);
        parcelle.setNom("Parcelle Test");
        parcelle.setSuperficie(5000.0);
        parcelle.setCulture("Tomates");

        parcelleDTO = new ParcelleDTO();
        parcelleDTO.setNom("Parcelle Test");
        parcelleDTO.setSuperficie(5000.0);
        parcelleDTO.setCulture("Tomates");
    }

    @Test
    void testCreateParcelle() {
        when(parcelleRepository.save(any(Parcelle.class))).thenReturn(parcelle);

        ParcelleDTO result = parcelleService.createParcelle(parcelleDTO);

        assertNotNull(result);
        assertEquals("Parcelle Test", result.getNom());
        assertEquals(5000.0, result.getSuperficie());
        assertEquals("Tomates", result.getCulture());
        verify(parcelleRepository, times(1)).save(any(Parcelle.class));
    }

    @Test
    void testGetParcelleById_Success() {
        when(parcelleRepository.findById(1L)).thenReturn(Optional.of(parcelle));

        ParcelleDTO result = parcelleService.getParcelleById(1L);

        assertNotNull(result);
        assertEquals("Parcelle Test", result.getNom());
        verify(parcelleRepository, times(1)).findById(1L);
    }

    @Test
    void testGetParcelleById_NotFound() {
        when(parcelleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            parcelleService.getParcelleById(1L);
        });
        verify(parcelleRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllParcelles() {
        List<Parcelle> parcelles = Arrays.asList(parcelle);
        when(parcelleRepository.findAll()).thenReturn(parcelles);

        List<ParcelleDTO> results = parcelleService.getAllParcelles();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Parcelle Test", results.get(0).getNom());
        verify(parcelleRepository, times(1)).findAll();
    }

    @Test
    void testGetParcellesByCulture() {
        List<Parcelle> parcelles = Arrays.asList(parcelle);
        when(parcelleRepository.findByCulture("Tomates")).thenReturn(parcelles);

        List<ParcelleDTO> results = parcelleService.getParcellesByCulture("Tomates");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Tomates", results.get(0).getCulture());
        verify(parcelleRepository, times(1)).findByCulture("Tomates");
    }

    @Test
    void testUpdateParcelle() {
        when(parcelleRepository.findById(1L)).thenReturn(Optional.of(parcelle));
        when(parcelleRepository.save(any(Parcelle.class))).thenReturn(parcelle);

        parcelleDTO.setNom("Parcelle Modifiée");
        ParcelleDTO result = parcelleService.updateParcelle(1L, parcelleDTO);

        assertNotNull(result);
        assertEquals("Parcelle Modifiée", result.getNom());
        verify(parcelleRepository, times(1)).findById(1L);
        verify(parcelleRepository, times(1)).save(any(Parcelle.class));
    }

    @Test
    void testDeleteParcelle() {
        when(parcelleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(parcelleRepository).deleteById(1L);

        parcelleService.deleteParcelle(1L);

        verify(parcelleRepository, times(1)).existsById(1L);
        verify(parcelleRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteParcelle_NotFound() {
        when(parcelleRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            parcelleService.deleteParcelle(1L);
        });
        verify(parcelleRepository, times(1)).existsById(1L);
        verify(parcelleRepository, never()).deleteById(1L);
    }
}
