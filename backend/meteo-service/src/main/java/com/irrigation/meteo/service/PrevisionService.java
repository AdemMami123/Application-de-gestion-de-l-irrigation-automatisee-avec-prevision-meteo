package com.irrigation.meteo.service;

import com.irrigation.meteo.dto.PrevisionDTO;
import com.irrigation.meteo.entity.Prevision;
import com.irrigation.meteo.entity.StationMeteo;
import com.irrigation.meteo.event.WeatherChangeEvent;
import com.irrigation.meteo.event.WeatherChangeEvent.WeatherConditions;
import com.irrigation.meteo.kafka.KafkaWeatherProducer;
import com.irrigation.meteo.repository.PrevisionRepository;
import com.irrigation.meteo.repository.StationMeteoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des prévisions météo
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrevisionService {

    private final PrevisionRepository previsionRepository;
    private final StationMeteoRepository stationMeteoRepository;
    private final KafkaWeatherProducer kafkaWeatherProducer;

    /**
     * Créer une nouvelle prévision
     */
    public PrevisionDTO create(PrevisionDTO dto) {
        log.info("Création d'une nouvelle prévision pour la station ID: {}", dto.getStationId());
        StationMeteo station = stationMeteoRepository.findById(dto.getStationId())
                .orElseThrow(() -> new RuntimeException("Station météo non trouvée avec l'ID: " + dto.getStationId()));
        
        Prevision prevision = mapToEntity(dto, station);
        Prevision savedPrevision = previsionRepository.save(prevision);
        return mapToDTO(savedPrevision);
    }

    /**
     * Récupérer toutes les prévisions
     */
    @Transactional(readOnly = true)
    public List<PrevisionDTO> findAll() {
        log.info("Récupération de toutes les prévisions");
        return previsionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une prévision par son ID
     */
    @Transactional(readOnly = true)
    public PrevisionDTO findById(Long id) {
        log.info("Récupération de la prévision avec l'ID: {}", id);
        return previsionRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Prévision non trouvée avec l'ID: " + id));
    }

    /**
     * Récupérer les prévisions d'une station
     */
    @Transactional(readOnly = true)
    public List<PrevisionDTO> findByStationId(Long stationId) {
        log.info("Récupération des prévisions pour la station ID: {}", stationId);
        return previsionRepository.findByStationId(stationId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les prévisions entre deux dates
     */
    @Transactional(readOnly = true)
    public List<PrevisionDTO> findByStationIdAndDateBetween(Long stationId, LocalDate startDate, LocalDate endDate) {
        log.info("Récupération des prévisions pour la station ID: {} entre {} et {}", stationId, startDate, endDate);
        return previsionRepository.findByStationIdAndDateBetween(stationId, startDate, endDate).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour une prévision
     */
    public PrevisionDTO update(Long id, PrevisionDTO dto) {
        log.info("Mise à jour de la prévision avec l'ID: {}", id);
        Prevision prevision = previsionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prévision non trouvée avec l'ID: " + id));
        
        // Capture old conditions before update
        WeatherConditions oldConditions = createWeatherConditions(prevision);
        
        if (dto.getStationId() != null && !dto.getStationId().equals(prevision.getStation().getId())) {
            StationMeteo station = stationMeteoRepository.findById(dto.getStationId())
                    .orElseThrow(() -> new RuntimeException("Station météo non trouvée avec l'ID: " + dto.getStationId()));
            prevision.setStation(station);
        }
        
        prevision.setDate(dto.getDate());
        prevision.setTemperatureMax(dto.getTemperatureMax());
        prevision.setTemperatureMin(dto.getTemperatureMin());
        prevision.setPluiePrevue(dto.getPluiePrevue());
        prevision.setVent(dto.getVent());
        
        Prevision updatedPrevision = previsionRepository.save(prevision);
        
        // Check for significant changes and publish event
        WeatherConditions newConditions = createWeatherConditions(updatedPrevision);
        detectAndPublishWeatherChange(updatedPrevision.getStation(), oldConditions, newConditions);
        
        return mapToDTO(updatedPrevision);
    }

    /**
     * Supprimer une prévision
     */
    public void delete(Long id) {
        log.info("Suppression de la prévision avec l'ID: {}", id);
        if (!previsionRepository.existsById(id)) {
            throw new RuntimeException("Prévision non trouvée avec l'ID: " + id);
        }
        previsionRepository.deleteById(id);
    }
    
    /**
     * Détecter les changements significatifs et publier un événement Kafka
     */
    private void detectAndPublishWeatherChange(StationMeteo station, WeatherConditions oldConditions, WeatherConditions newConditions) {
        WeatherChangeEvent.ChangeSeverity severity = WeatherChangeEvent.calculateSeverity(oldConditions, newConditions);
        
        // Only publish events for MEDIUM, HIGH, or CRITICAL severity
        if (severity == WeatherChangeEvent.ChangeSeverity.LOW) {
            log.debug("Changement météo de faible sévérité détecté pour la station {} - Événement non publié", station.getId());
            return;
        }
        
        WeatherChangeEvent event = new WeatherChangeEvent(
                station.getId(),
                station.getNom(),
                oldConditions,
                newConditions,
                LocalDateTime.now(),
                severity,
                WeatherChangeEvent.generateDescription(oldConditions, newConditions)
        );
        
        log.info("Changement météo significatif détecté pour la station {} avec sévérité {} - Publication de l'événement", 
                station.getId(), severity);
        
        kafkaWeatherProducer.publishWeatherChange(event);
    }
    
    /**
     * Créer un objet WeatherConditions à partir d'une Prevision
     */
    private WeatherConditions createWeatherConditions(Prevision prevision) {
        return new WeatherConditions(
                prevision.getTemperatureMax() != null ? prevision.getTemperatureMax().doubleValue() : null,
                prevision.getTemperatureMin() != null ? prevision.getTemperatureMin().doubleValue() : null,
                prevision.getPluiePrevue() != null ? prevision.getPluiePrevue().doubleValue() : null,
                prevision.getVent() != null ? prevision.getVent().doubleValue() : null,
                prevision.getDate().atStartOfDay()
        );
    }

    // Mapping methods
    private Prevision mapToEntity(PrevisionDTO dto, StationMeteo station) {
        return Prevision.builder()
                .id(dto.getId())
                .station(station)
                .date(dto.getDate())
                .temperatureMax(dto.getTemperatureMax())
                .temperatureMin(dto.getTemperatureMin())
                .pluiePrevue(dto.getPluiePrevue())
                .vent(dto.getVent())
                .build();
    }

    private PrevisionDTO mapToDTO(Prevision entity) {
        return PrevisionDTO.builder()
                .id(entity.getId())
                .stationId(entity.getStation().getId())
                .stationNom(entity.getStation().getNom())
                .date(entity.getDate())
                .temperatureMax(entity.getTemperatureMax())
                .temperatureMin(entity.getTemperatureMin())
                .pluiePrevue(entity.getPluiePrevue())
                .vent(entity.getVent())
                .build();
    }
}
