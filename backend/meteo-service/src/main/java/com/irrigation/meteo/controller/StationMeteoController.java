package com.irrigation.meteo.controller;

import com.irrigation.meteo.dto.StationMeteoDTO;
import com.irrigation.meteo.service.StationMeteoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des stations météo
 */
@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
@Tag(name = "Stations Météo", description = "API de gestion des stations météorologiques")
public class StationMeteoController {

    private final StationMeteoService stationMeteoService;

    @PostMapping
    @Operation(summary = "Créer une station météo", description = "Crée une nouvelle station météorologique")
    public ResponseEntity<StationMeteoDTO> createStation(@Valid @RequestBody StationMeteoDTO stationDTO) {
        StationMeteoDTO created = stationMeteoService.create(stationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les stations", description = "Récupère toutes les stations météorologiques")
    public ResponseEntity<List<StationMeteoDTO>> getAllStations() {
        List<StationMeteoDTO> stations = stationMeteoService.findAll();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une station", description = "Récupère une station météorologique par son ID")
    public ResponseEntity<StationMeteoDTO> getStationById(@PathVariable Long id) {
        StationMeteoDTO station = stationMeteoService.findById(id);
        return ResponseEntity.ok(station);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une station", description = "Met à jour une station météorologique existante")
    public ResponseEntity<StationMeteoDTO> updateStation(
            @PathVariable Long id,
            @Valid @RequestBody StationMeteoDTO stationDTO) {
        StationMeteoDTO updated = stationMeteoService.update(id, stationDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une station", description = "Supprime une station météorologique")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationMeteoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fournisseur/{fournisseur}")
    @Operation(summary = "Rechercher par fournisseur", description = "Récupère toutes les stations d'un fournisseur")
    public ResponseEntity<List<StationMeteoDTO>> getStationsByFournisseur(@PathVariable String fournisseur) {
        List<StationMeteoDTO> stations = stationMeteoService.findByFournisseur(fournisseur);
        return ResponseEntity.ok(stations);
    }
}
