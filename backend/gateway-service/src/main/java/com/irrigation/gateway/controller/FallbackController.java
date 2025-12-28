package com.irrigation.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de fallback pour les circuit breakers
 * Retourne des réponses par défaut quand un service est indisponible
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/meteo")
    public ResponseEntity<Map<String, Object>> meteoFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Le service météo est temporairement indisponible. Veuillez réessayer plus tard.");
        response.put("service", "meteo-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/arrosage")
    public ResponseEntity<Map<String, Object>> arrosageFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Le service d'arrosage est temporairement indisponible. Veuillez réessayer plus tard.");
        response.put("service", "arrosage-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Authentication service is temporarily unavailable. Please try again later.");
        response.put("service", "auth-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/default")
    public ResponseEntity<Map<String, Object>> defaultFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Le service demandé est temporairement indisponible. Veuillez réessayer plus tard.");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
