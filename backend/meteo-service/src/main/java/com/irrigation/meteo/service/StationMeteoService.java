package com.irrigation.meteo.service;

import com.irrigation.meteo.dto.StationMeteoDTO;
import com.irrigation.meteo.entity.StationMeteo;
import com.irrigation.meteo.repository.StationMeteoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des stations météo
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StationMeteoService {

    private final StationMeteoRepository stationMeteoRepository;

    /**
     * Créer une nouvelle station météo
     */
    public StationMeteoDTO create(StationMeteoDTO dto) {
        log.info("Création d'une nouvelle station météo: {}", dto.getNom());
        StationMeteo station = mapToEntity(dto);
        StationMeteo savedStation = stationMeteoRepository.save(station);
        return mapToDTO(savedStation);
    }

    /**
     * Récupérer toutes les stations météo
     */
    @Transactional(readOnly = true)
    public List<StationMeteoDTO> findAll() {
        log.info("Récupération de toutes les stations météo");
        return stationMeteoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une station par son ID
     */
    @Transactional(readOnly = true)
    public StationMeteoDTO findById(Long id) {
        log.info("Récupération de la station météo avec l'ID: {}", id);
        return stationMeteoRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Station météo non trouvée avec l'ID: " + id));
    }

    /**
     * Mettre à jour une station météo
     */
    public StationMeteoDTO update(Long id, StationMeteoDTO dto) {
        log.info("Mise à jour de la station météo avec l'ID: {}", id);
        StationMeteo station = stationMeteoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station météo non trouvée avec l'ID: " + id));
        
        station.setNom(dto.getNom());
        station.setLatitude(dto.getLatitude());
        station.setLongitude(dto.getLongitude());
        station.setFournisseur(dto.getFournisseur());
        
        StationMeteo updatedStation = stationMeteoRepository.save(station);
        return mapToDTO(updatedStation);
    }

    /**
     * Supprimer une station météo
     */
    public void delete(Long id) {
        log.info("Suppression de la station météo avec l'ID: {}", id);
        if (!stationMeteoRepository.existsById(id)) {
            throw new RuntimeException("Station météo non trouvée avec l'ID: " + id);
        }
        stationMeteoRepository.deleteById(id);
    }

    /**
     * Rechercher par fournisseur
     */
    @Transactional(readOnly = true)
    public List<StationMeteoDTO> findByFournisseur(String fournisseur) {
        log.info("Récupération des stations météo du fournisseur: {}", fournisseur);
        return stationMeteoRepository.findByFournisseur(fournisseur).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Mapping methods
    private StationMeteo mapToEntity(StationMeteoDTO dto) {
        return StationMeteo.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .fournisseur(dto.getFournisseur())
                .build();
    }

    private StationMeteoDTO mapToDTO(StationMeteo entity) {
        return StationMeteoDTO.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .fournisseur(entity.getFournisseur())
                .build();
    }
}
