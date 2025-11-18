package com.irrigation.arrosage.service;

import com.irrigation.arrosage.entity.ProgrammeArrosage;
import com.irrigation.arrosage.entity.StatutProgramme;
import com.irrigation.arrosage.event.WeatherChangeEvent;
import com.irrigation.arrosage.event.WeatherChangeEvent.WeatherConditions;
import com.irrigation.arrosage.repository.ProgrammeArrosageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WeatherBasedSchedulingService {

    private final ProgrammeArrosageRepository programmeRepository;

    /**
     * Handle CRITICAL severity weather changes
     * Cancel or reschedule programmes immediately
     */
    public void handleCriticalWeatherChange(WeatherChangeEvent event) {
        log.warn("Handling CRITICAL weather change for station {}: {}", 
                event.getStationId(), event.getDescription());
        
        LocalDateTime eventDate = event.getNewConditions().getDate();
        List<ProgrammeArrosage> affectedProgrammes = findAffectedProgrammes(eventDate);
        
        WeatherConditions newConditions = event.getNewConditions();
        
        for (ProgrammeArrosage programme : affectedProgrammes) {
            // Cancel if heavy rain expected
            if (newConditions.getPluiePrevue() != null && newConditions.getPluiePrevue() > 20) {
                programme.setStatut(StatutProgramme.ANNULE);
                log.info("Programme {} CANCELLED due to heavy rain forecast ({}mm)", 
                        programme.getId(), newConditions.getPluiePrevue());
            }
            // Reschedule if extreme temperature change
            else if (hasExtremeTemperatureChange(event)) {
                adjustProgrammeForTemperature(programme, newConditions);
                log.info("Programme {} ADJUSTED for extreme temperature change", programme.getId());
            }
            // Adjust for high wind
            else if (newConditions.getVent() != null && newConditions.getVent() > 30) {
                programme.setDuree((int) (programme.getDuree() * 1.4)); // Increase duration by 40%
                log.info("Programme {} duration increased by 40% due to high wind", programme.getId());
            }
        }
        
        programmeRepository.saveAll(affectedProgrammes);
        log.info("Processed {} programmes for CRITICAL weather change", affectedProgrammes.size());
    }

    /**
     * Handle HIGH severity weather changes
     * Adjust irrigation volumes and durations
     */
    public void handleHighSeverityWeatherChange(WeatherChangeEvent event) {
        log.info("Handling HIGH severity weather change for station {}: {}", 
                event.getStationId(), event.getDescription());
        
        LocalDateTime eventDate = event.getNewConditions().getDate();
        List<ProgrammeArrosage> affectedProgrammes = findAffectedProgrammes(eventDate);
        
        WeatherConditions oldConditions = event.getOldConditions();
        WeatherConditions newConditions = event.getNewConditions();
        
        for (ProgrammeArrosage programme : affectedProgrammes) {
            // Adjust for rain
            if (hasSignificantRainIncrease(oldConditions, newConditions)) {
                double rainDiff = newConditions.getPluiePrevue() - oldConditions.getPluiePrevue();
                double reductionFactor = Math.min(0.5, rainDiff / 20.0); // Max 50% reduction
                programme.setVolumePrevu(programme.getVolumePrevu() * (1 - reductionFactor));
                log.info("Programme {} volume reduced by {}% due to rain forecast", 
                        programme.getId(), (int)(reductionFactor * 100));
            }
            // Adjust for temperature
            else if (hasSignificantTemperatureChange(event)) {
                adjustProgrammeForTemperature(programme, newConditions);
            }
            // Adjust for wind
            else if (hasSignificantWindIncrease(oldConditions, newConditions)) {
                programme.setDuree((int) (programme.getDuree() * 1.2)); // Increase duration by 20%
                log.info("Programme {} duration increased by 20% due to wind", programme.getId());
            }
        }
        
        programmeRepository.saveAll(affectedProgrammes);
        log.info("Processed {} programmes for HIGH severity weather change", affectedProgrammes.size());
    }

    /**
     * Handle MEDIUM severity weather changes
     * Minor adjustments to schedules
     */
    public void handleMediumSeverityWeatherChange(WeatherChangeEvent event) {
        log.info("Handling MEDIUM severity weather change for station {}: {}", 
                event.getStationId(), event.getDescription());
        
        LocalDateTime eventDate = event.getNewConditions().getDate();
        List<ProgrammeArrosage> affectedProgrammes = findAffectedProgrammes(eventDate);
        
        WeatherConditions oldConditions = event.getOldConditions();
        WeatherConditions newConditions = event.getNewConditions();
        
        for (ProgrammeArrosage programme : affectedProgrammes) {
            // Minor rain adjustment
            if (newConditions.getPluiePrevue() != null && 
                oldConditions.getPluiePrevue() != null &&
                newConditions.getPluiePrevue() > oldConditions.getPluiePrevue() + 5) {
                
                programme.setVolumePrevu(programme.getVolumePrevu() * 0.9); // Reduce by 10%
                log.info("Programme {} volume reduced by 10% due to moderate rain increase", 
                        programme.getId());
            }
            // Minor wind adjustment
            else if (newConditions.getVent() != null &&
                     oldConditions.getVent() != null &&
                     newConditions.getVent() > oldConditions.getVent() + 7) {
                
                programme.setDuree((int) (programme.getDuree() * 1.1)); // Increase by 10%
                log.info("Programme {} duration increased by 10% due to moderate wind increase", 
                        programme.getId());
            }
        }
        
        programmeRepository.saveAll(affectedProgrammes);
        log.info("Processed {} programmes for MEDIUM severity weather change", affectedProgrammes.size());
    }

    /**
     * Find programmes affected by the weather change
     * Returns programmes scheduled within 24 hours of the event date
     */
    private List<ProgrammeArrosage> findAffectedProgrammes(LocalDateTime eventDate) {
        LocalDateTime startDate = eventDate.minusHours(12);
        LocalDateTime endDate = eventDate.plusHours(36);
        
        return programmeRepository.findByDatePlanifieeBetweenAndStatut(
                startDate, 
                endDate, 
                StatutProgramme.PLANIFIE
        );
    }

    private boolean hasExtremeTemperatureChange(WeatherChangeEvent event) {
        WeatherConditions old = event.getOldConditions();
        WeatherConditions newC = event.getNewConditions();
        
        if (old.getTemperatureMax() == null || newC.getTemperatureMax() == null) {
            return false;
        }
        
        return Math.abs(newC.getTemperatureMax() - old.getTemperatureMax()) > 10;
    }

    private boolean hasSignificantTemperatureChange(WeatherChangeEvent event) {
        WeatherConditions old = event.getOldConditions();
        WeatherConditions newC = event.getNewConditions();
        
        if (old.getTemperatureMax() == null || newC.getTemperatureMax() == null) {
            return false;
        }
        
        return Math.abs(newC.getTemperatureMax() - old.getTemperatureMax()) > 5;
    }

    private boolean hasSignificantRainIncrease(WeatherConditions old, WeatherConditions newC) {
        if (old.getPluiePrevue() == null || newC.getPluiePrevue() == null) {
            return false;
        }
        
        return newC.getPluiePrevue() > old.getPluiePrevue() + 10;
    }

    private boolean hasSignificantWindIncrease(WeatherConditions old, WeatherConditions newC) {
        if (old.getVent() == null || newC.getVent() == null) {
            return false;
        }
        
        return newC.getVent() > old.getVent() + 10;
    }

    private void adjustProgrammeForTemperature(ProgrammeArrosage programme, WeatherConditions conditions) {
        if (conditions.getTemperatureMax() == null) {
            return;
        }
        
        // Increase water volume for higher temperatures
        if (conditions.getTemperatureMax() > 30) {
            programme.setVolumePrevu(programme.getVolumePrevu() * 1.3); // +30%
            programme.setDuree((int) (programme.getDuree() * 1.2)); // +20%
        } else if (conditions.getTemperatureMax() > 25) {
            programme.setVolumePrevu(programme.getVolumePrevu() * 1.15); // +15%
        } else if (conditions.getTemperatureMax() < 15) {
            programme.setVolumePrevu(programme.getVolumePrevu() * 0.85); // -15%
        }
    }
}
