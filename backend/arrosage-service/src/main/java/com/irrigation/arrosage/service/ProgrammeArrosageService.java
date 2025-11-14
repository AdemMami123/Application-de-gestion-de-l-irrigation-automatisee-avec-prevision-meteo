package com.irrigation.arrosage.service;

import com.irrigation.arrosage.client.MeteoServiceClient;
import com.irrigation.arrosage.dto.PrevisionMeteoDTO;
import com.irrigation.arrosage.dto.ProgrammeArrosageDTO;
import com.irrigation.arrosage.entity.Parcelle;
import com.irrigation.arrosage.entity.ProgrammeArrosage;
import com.irrigation.arrosage.repository.ParcelleRepository;
import com.irrigation.arrosage.repository.ProgrammeArrosageRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProgrammeArrosageService {

    private final ProgrammeArrosageRepository programmeRepository;
    private final ParcelleRepository parcelleRepository;
    private final MeteoServiceClient meteoServiceClient;

    public ProgrammeArrosageDTO create(ProgrammeArrosageDTO dto) {
        log.info("Création d'un nouveau programme d'arrosage pour la parcelle ID: {}", dto.getParcelleId());
        Parcelle parcelle = parcelleRepository.findById(dto.getParcelleId())
                .orElseThrow(() -> new RuntimeException("Parcelle non trouvée avec l'ID: " + dto.getParcelleId()));
        
        ProgrammeArrosage programme = mapToEntity(dto, parcelle);
        ProgrammeArrosage saved = programmeRepository.save(programme);
        return mapToDTO(saved);
    }

    /**
     * Planifier l'arrosage en fonction des prévisions météo
     */
    @CircuitBreaker(name = "meteoService", fallbackMethod = "scheduleIrrigationFallback")
    public ProgrammeArrosageDTO scheduleIrrigationBasedOnWeather(Long parcelleId, Long stationId, LocalDateTime datePlanifiee) {
        log.info("Planification intelligente de l'arrosage pour la parcelle {} basée sur la météo", parcelleId);
        
        Parcelle parcelle = parcelleRepository.findById(parcelleId)
                .orElseThrow(() -> new RuntimeException("Parcelle non trouvée avec l'ID: " + parcelleId));
        
        // Récupérer les prévisions météo pour les 3 prochains jours
        LocalDate startDate = datePlanifiee.toLocalDate();
        LocalDate endDate = startDate.plusDays(3);
        
        List<PrevisionMeteoDTO> previsions = meteoServiceClient.getPrevisionsByPeriode(stationId, startDate, endDate);
        
        // Calculer les besoins en eau basés sur les prévisions
        BigDecimal pluiePrevueTotale = previsions.stream()
                .map(p -> p.getPluiePrevue() != null ? p.getPluiePrevue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        log.info("Pluie prévue totale: {} mm", pluiePrevueTotale);
        
        // Ajuster le volume et la durée selon les prévisions
        BigDecimal volumePrevu = calculateIrrigationVolume(parcelle, pluiePrevueTotale);
        Integer duree = calculateIrrigationDuration(parcelle, volumePrevu);
        
        ProgrammeArrosage programme = ProgrammeArrosage.builder()
                .parcelle(parcelle)
                .datePlanifiee(datePlanifiee)
                .duree(duree)
                .volumePrevu(volumePrevu)
                .statut(ProgrammeArrosage.StatutProgramme.PLANIFIE)
                .build();
        
        ProgrammeArrosage saved = programmeRepository.save(programme);
        log.info("Programme créé avec volume: {} m³ et durée: {} min", volumePrevu, duree);
        
        return mapToDTO(saved);
    }

    /**
     * Fallback en cas d'échec du service météo
     */
    public ProgrammeArrosageDTO scheduleIrrigationFallback(Long parcelleId, Long stationId, LocalDateTime datePlanifiee, Exception ex) {
        log.warn("Service météo indisponible, utilisation des valeurs par défaut. Erreur: {}", ex.getMessage());
        
        Parcelle parcelle = parcelleRepository.findById(parcelleId)
                .orElseThrow(() -> new RuntimeException("Parcelle non trouvée avec l'ID: " + parcelleId));
        
        // Valeurs par défaut en cas d'indisponibilité du service météo
        BigDecimal volumePrevu = calculateIrrigationVolume(parcelle, BigDecimal.ZERO);
        Integer duree = calculateIrrigationDuration(parcelle, volumePrevu);
        
        ProgrammeArrosage programme = ProgrammeArrosage.builder()
                .parcelle(parcelle)
                .datePlanifiee(datePlanifiee)
                .duree(duree)
                .volumePrevu(volumePrevu)
                .statut(ProgrammeArrosage.StatutProgramme.PLANIFIE)
                .build();
        
        ProgrammeArrosage saved = programmeRepository.save(programme);
        return mapToDTO(saved);
    }

    private BigDecimal calculateIrrigationVolume(Parcelle parcelle, BigDecimal pluiePrevue) {
        // Calcul simplifié: 5mm d'eau par m² - pluie prévue
        BigDecimal besoinsBase = new BigDecimal("5.0");
        BigDecimal besoinsAjustes = besoinsBase.subtract(pluiePrevue).max(BigDecimal.ZERO);
        
        // Volume en m³ = superficie (m²) * besoin en eau (mm) / 1000
        return parcelle.getSuperficie()
                .multiply(besoinsAjustes)
                .divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);
    }

    private Integer calculateIrrigationDuration(Parcelle parcelle, BigDecimal volume) {
        // Durée en minutes basée sur un débit de 0.5 m³/min
        BigDecimal debit = new BigDecimal("0.5");
        return volume.divide(debit, 0, BigDecimal.ROUND_UP).intValue();
    }

    @Transactional(readOnly = true)
    public List<ProgrammeArrosageDTO> findAll() {
        return programmeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProgrammeArrosageDTO findById(Long id) {
        return programmeRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Programme non trouvé avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProgrammeArrosageDTO> findByParcelleId(Long parcelleId) {
        return programmeRepository.findByParcelleId(parcelleId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgrammeArrosageDTO> findByStatut(ProgrammeArrosage.StatutProgramme statut) {
        return programmeRepository.findByStatut(statut).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProgrammeArrosageDTO update(Long id, ProgrammeArrosageDTO dto) {
        ProgrammeArrosage programme = programmeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programme non trouvé avec l'ID: " + id));
        
        if (dto.getParcelleId() != null && !dto.getParcelleId().equals(programme.getParcelle().getId())) {
            Parcelle parcelle = parcelleRepository.findById(dto.getParcelleId())
                    .orElseThrow(() -> new RuntimeException("Parcelle non trouvée avec l'ID: " + dto.getParcelleId()));
            programme.setParcelle(parcelle);
        }
        
        programme.setDatePlanifiee(dto.getDatePlanifiee());
        programme.setDuree(dto.getDuree());
        programme.setVolumePrevu(dto.getVolumePrevu());
        programme.setStatut(dto.getStatut());
        
        ProgrammeArrosage updated = programmeRepository.save(programme);
        return mapToDTO(updated);
    }

    public void delete(Long id) {
        if (!programmeRepository.existsById(id)) {
            throw new RuntimeException("Programme non trouvé avec l'ID: " + id);
        }
        programmeRepository.deleteById(id);
    }

    private ProgrammeArrosage mapToEntity(ProgrammeArrosageDTO dto, Parcelle parcelle) {
        return ProgrammeArrosage.builder()
                .id(dto.getId())
                .parcelle(parcelle)
                .datePlanifiee(dto.getDatePlanifiee())
                .duree(dto.getDuree())
                .volumePrevu(dto.getVolumePrevu())
                .statut(dto.getStatut())
                .build();
    }

    private ProgrammeArrosageDTO mapToDTO(ProgrammeArrosage entity) {
        return ProgrammeArrosageDTO.builder()
                .id(entity.getId())
                .parcelleId(entity.getParcelle().getId())
                .parcelleNom(entity.getParcelle().getNom())
                .datePlanifiee(entity.getDatePlanifiee())
                .duree(entity.getDuree())
                .volumePrevu(entity.getVolumePrevu())
                .statut(entity.getStatut())
                .build();
    }
}
