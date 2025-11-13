package com.irrigation.meteo.controller;

import com.irrigation.meteo.dto.PrevisionDTO;
import com.irrigation.meteo.service.PrevisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des prévisions météo
 */
@RestController
@RequestMapping("/api/previsions")
@RequiredArgsConstructor
@Tag(name = "Prévisions Météo", description = "API de gestion des prévisions météorologiques")
public class PrevisionController {

    private final PrevisionService previsionService;

    @PostMapping
    @Operation(summary = "Créer une prévision", description = "Crée une nouvelle prévision météorologique")
    public ResponseEntity<PrevisionDTO> createPrevision(@Valid @RequestBody PrevisionDTO previsionDTO) {
        PrevisionDTO created = previsionService.create(previsionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les prévisions", description = "Récupère toutes les prévisions météorologiques")
    public ResponseEntity<List<PrevisionDTO>> getAllPrevisions() {
        List<PrevisionDTO> previsions = previsionService.findAll();
        return ResponseEntity.ok(previsions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une prévision", description = "Récupère une prévision météorologique par son ID")
    public ResponseEntity<PrevisionDTO> getPrevisionById(@PathVariable Long id) {
        PrevisionDTO prevision = previsionService.findById(id);
        return ResponseEntity.ok(prevision);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une prévision", description = "Met à jour une prévision météorologique existante")
    public ResponseEntity<PrevisionDTO> updatePrevision(
            @PathVariable Long id,
            @Valid @RequestBody PrevisionDTO previsionDTO) {
        PrevisionDTO updated = previsionService.update(id, previsionDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une prévision", description = "Supprime une prévision météorologique")
    public ResponseEntity<Void> deletePrevision(@PathVariable Long id) {
        previsionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/station/{stationId}")
    @Operation(summary = "Prévisions par station", description = "Récupère toutes les prévisions d'une station")
    public ResponseEntity<List<PrevisionDTO>> getPrevisionsByStation(@PathVariable Long stationId) {
        List<PrevisionDTO> previsions = previsionService.findByStationId(stationId);
        return ResponseEntity.ok(previsions);
    }

    @GetMapping("/station/{stationId}/periode")
    @Operation(summary = "Prévisions par période", description = "Récupère les prévisions d'une station entre deux dates")
    public ResponseEntity<List<PrevisionDTO>> getPrevisionsByPeriode(
            @PathVariable Long stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PrevisionDTO> previsions = previsionService.findByStationIdAndDateBetween(stationId, startDate, endDate);
        return ResponseEntity.ok(previsions);
    }
}
