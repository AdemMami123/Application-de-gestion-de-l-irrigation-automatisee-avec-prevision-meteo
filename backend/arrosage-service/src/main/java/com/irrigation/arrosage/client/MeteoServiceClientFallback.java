package com.irrigation.arrosage.client;

import com.irrigation.arrosage.dto.PrevisionMeteoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Fallback pour le client Météo en cas d'échec (Circuit Breaker)
 */
@Component
@Slf4j
public class MeteoServiceClientFallback implements MeteoServiceClient {
    
    @Override
    public List<PrevisionMeteoDTO> getAllPrevisions() {
        log.warn("Fallback activé pour getAllPrevisions - Service météo indisponible");
        return Collections.emptyList();
    }
    
    @Override
    public PrevisionMeteoDTO getPrevisionById(Long id) {
        log.warn("Fallback activé pour getPrevisionById - Service météo indisponible");
        return null;
    }
    
    @Override
    public List<PrevisionMeteoDTO> getPrevisionsByStation(Long stationId) {
        log.warn("Fallback activé pour getPrevisionsByStation - Service météo indisponible");
        return Collections.emptyList();
    }
    
    @Override
    public List<PrevisionMeteoDTO> getPrevisionsByPeriode(Long stationId, LocalDate startDate, LocalDate endDate) {
        log.warn("Fallback activé pour getPrevisionsByPeriode - Service météo indisponible");
        return Collections.emptyList();
    }
}
