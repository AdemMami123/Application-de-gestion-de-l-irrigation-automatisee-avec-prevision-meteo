package com.irrigation.arrosage.client;

import com.irrigation.arrosage.dto.PrevisionMeteoDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Client Feign pour communiquer avec le microservice météo
 * Avec cache Redis pour optimiser les performances
 */
@FeignClient(
    name = "meteo-service",
    fallback = MeteoServiceClientFallback.class
)
public interface MeteoServiceClient {
    
    /**
     * Récupère toutes les prévisions météo
     * Cache: 2 heures
     */
    @GetMapping("/api/previsions")
    @Cacheable(value = "forecasts", key = "'all'", unless = "#result == null or #result.isEmpty()")
    List<PrevisionMeteoDTO> getAllPrevisions();
    
    /**
     * Récupère une prévision par ID
     * Cache: 2 heures
     */
    @GetMapping("/api/previsions/{id}")
    @Cacheable(value = "forecasts", key = "'id:' + #id", unless = "#result == null")
    PrevisionMeteoDTO getPrevisionById(@PathVariable("id") Long id);
    
    /**
     * Récupère les prévisions pour une station
     * Cache: 2 heures avec clé basée sur l'ID de station
     */
    @GetMapping("/api/previsions/station/{stationId}")
    @Cacheable(value = "forecasts", key = "'station:' + #stationId", unless = "#result == null or #result.isEmpty()")
    List<PrevisionMeteoDTO> getPrevisionsByStation(@PathVariable("stationId") Long stationId);
    
    /**
     * Récupère les prévisions pour une station sur une période
     * Cache: 2 heures avec clé composite (station + dates)
     */
    @GetMapping("/api/previsions/station/{stationId}/periode")
    @Cacheable(
        value = "forecasts", 
        key = "'station:' + #stationId + ':' + #startDate + ':' + #endDate",
        unless = "#result == null or #result.isEmpty()"
    )
    List<PrevisionMeteoDTO> getPrevisionsByPeriode(
            @PathVariable("stationId") Long stationId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );
}
