package com.irrigation.arrosage.service;

import com.irrigation.arrosage.dto.ParcelleDTO;
import com.irrigation.arrosage.entity.Parcelle;
import com.irrigation.arrosage.repository.ParcelleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParcelleService {

    private final ParcelleRepository parcelleRepository;

    public ParcelleDTO create(ParcelleDTO dto) {
        log.info("Création d'une nouvelle parcelle: {}", dto.getNom());
        Parcelle parcelle = mapToEntity(dto);
        Parcelle saved = parcelleRepository.save(parcelle);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ParcelleDTO> findAll() {
        log.info("Récupération de toutes les parcelles");
        return parcelleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParcelleDTO findById(Long id) {
        log.info("Récupération de la parcelle avec l'ID: {}", id);
        return parcelleRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Parcelle non trouvée avec l'ID: " + id));
    }

    public ParcelleDTO update(Long id, ParcelleDTO dto) {
        log.info("Mise à jour de la parcelle avec l'ID: {}", id);
        Parcelle parcelle = parcelleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parcelle non trouvée avec l'ID: " + id));
        
        parcelle.setNom(dto.getNom());
        parcelle.setSuperficie(dto.getSuperficie());
        parcelle.setCulture(dto.getCulture());
        
        Parcelle updated = parcelleRepository.save(parcelle);
        return mapToDTO(updated);
    }

    public void delete(Long id) {
        log.info("Suppression de la parcelle avec l'ID: {}", id);
        if (!parcelleRepository.existsById(id)) {
            throw new RuntimeException("Parcelle non trouvée avec l'ID: " + id);
        }
        parcelleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ParcelleDTO> findByCulture(String culture) {
        log.info("Récupération des parcelles de culture: {}", culture);
        return parcelleRepository.findByCulture(culture).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private Parcelle mapToEntity(ParcelleDTO dto) {
        return Parcelle.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .superficie(dto.getSuperficie())
                .culture(dto.getCulture())
                .build();
    }

    private ParcelleDTO mapToDTO(Parcelle entity) {
        return ParcelleDTO.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .superficie(entity.getSuperficie())
                .culture(entity.getCulture())
                .build();
    }
}
