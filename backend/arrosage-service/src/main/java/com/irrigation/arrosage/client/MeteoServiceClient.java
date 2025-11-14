package com.irrigation.arrosage.client;

import com.irrigation.arrosage.dto.PrevisionMeteoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Client Feign pour communiquer avec le microservice météo
 */
@FeignClient(
    name = "meteo-service",
    fallback = MeteoServiceClientFallback.class
)
public interface MeteoServiceClient {
    
    @GetMapping("/api/previsions")
    List<PrevisionMeteoDTO> getAllPrevisions();
    
    @GetMapping("/api/previsions/{id}")
    PrevisionMeteoDTO getPrevisionById(@PathVariable("id") Long id);
    
    @GetMapping("/api/previsions/station/{stationId}")
    List<PrevisionMeteoDTO> getPrevisionsByStation(@PathVariable("stationId") Long stationId);
    
    @GetMapping("/api/previsions/station/{stationId}/periode")
    List<PrevisionMeteoDTO> getPrevisionsByPeriode(
            @PathVariable("stationId") Long stationId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );
}
