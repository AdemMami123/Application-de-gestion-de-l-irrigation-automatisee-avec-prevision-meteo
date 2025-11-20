package com.irrigation.arrosage.controller;

import com.irrigation.arrosage.service.CacheManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour la gestion du cache Redis
 */
@RestController
@RequestMapping("/api/cache")
@Tag(name = "Cache Management", description = "APIs pour la gestion du cache Redis")
public class CacheController {

    private final CacheManagementService cacheManagementService;

    public CacheController(CacheManagementService cacheManagementService) {
        this.cacheManagementService = cacheManagementService;
    }

    /**
     * Récupère les statistiques du cache
     */
    @GetMapping("/stats")
    @Operation(summary = "Récupérer les statistiques du cache")
    public ResponseEntity<CacheManagementService.CacheStats> getCacheStats() {
        return ResponseEntity.ok(cacheManagementService.getCacheStats());
    }

    /**
     * Vide tous les caches
     */
    @DeleteMapping("/evict/all")
    @Operation(summary = "Vider tous les caches")
    public ResponseEntity<Map<String, String>> evictAllCaches() {
        cacheManagementService.evictAllCaches();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All caches evicted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Vide le cache des prévisions météo
     */
    @DeleteMapping("/evict/forecasts")
    @Operation(summary = "Vider le cache des prévisions météo")
    public ResponseEntity<Map<String, String>> evictForecasts() {
        cacheManagementService.evictAllForecasts();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Forecasts cache evicted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Vide le cache pour une station spécifique
     */
    @DeleteMapping("/evict/forecasts/station/{stationId}")
    @Operation(summary = "Vider le cache pour une station spécifique")
    public ResponseEntity<Map<String, String>> evictForecastsForStation(@PathVariable Long stationId) {
        cacheManagementService.evictForecastsForStation(stationId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Forecasts cache for station " + stationId + " evicted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Vide le cache pour une période spécifique
     */
    @DeleteMapping("/evict/forecasts/station/{stationId}/period")
    @Operation(summary = "Vider le cache pour une période spécifique")
    public ResponseEntity<Map<String, String>> evictForecastsForPeriod(
            @PathVariable Long stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        cacheManagementService.evictForecastsForPeriod(stationId, startDate, endDate);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Forecasts cache for station " + stationId + 
                    " and period " + startDate + " to " + endDate + " evicted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Vérifie la disponibilité de Redis
     */
    @GetMapping("/health")
    @Operation(summary = "Vérifier la disponibilité de Redis")
    public ResponseEntity<Map<String, Object>> checkRedisHealth() {
        Map<String, Object> response = new HashMap<>();
        boolean available = cacheManagementService.isRedisAvailable();
        response.put("redis", available ? "UP" : "DOWN");
        response.put("available", available);
        return ResponseEntity.ok(response);
    }
}
