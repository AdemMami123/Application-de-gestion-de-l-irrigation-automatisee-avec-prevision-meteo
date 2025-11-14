package com.irrigation.arrosage.controller;

import com.irrigation.arrosage.dto.ProgrammeArrosageDTO;
import com.irrigation.arrosage.entity.ProgrammeArrosage;
import com.irrigation.arrosage.service.ProgrammeArrosageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/programmes")
@RequiredArgsConstructor
@Tag(name = "Programmes d'Arrosage", description = "API de gestion des programmes d'arrosage")
public class ProgrammeArrosageController {

    private final ProgrammeArrosageService programmeService;

    @PostMapping
    @Operation(summary = "Créer un programme", description = "Crée un nouveau programme d'arrosage")
    public ResponseEntity<ProgrammeArrosageDTO> createProgramme(@Valid @RequestBody ProgrammeArrosageDTO programmeDTO) {
        ProgrammeArrosageDTO created = programmeService.create(programmeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/schedule")
    @Operation(summary = "Planifier avec météo", description = "Planifie un arrosage basé sur les prévisions météo")
    public ResponseEntity<ProgrammeArrosageDTO> scheduleProgramme(
            @RequestParam Long parcelleId,
            @RequestParam Long stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime datePlanifiee) {
        ProgrammeArrosageDTO created = programmeService.scheduleIrrigationBasedOnWeather(parcelleId, stationId, datePlanifiee);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister tous les programmes", description = "Récupère tous les programmes d'arrosage")
    public ResponseEntity<List<ProgrammeArrosageDTO>> getAllProgrammes() {
        List<ProgrammeArrosageDTO> programmes = programmeService.findAll();
        return ResponseEntity.ok(programmes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un programme", description = "Récupère un programme par son ID")
    public ResponseEntity<ProgrammeArrosageDTO> getProgrammeById(@PathVariable Long id) {
        ProgrammeArrosageDTO programme = programmeService.findById(id);
        return ResponseEntity.ok(programme);
    }

    @GetMapping("/parcelle/{parcelleId}")
    @Operation(summary = "Programmes par parcelle", description = "Récupère tous les programmes d'une parcelle")
    public ResponseEntity<List<ProgrammeArrosageDTO>> getProgrammesByParcelle(@PathVariable Long parcelleId) {
        List<ProgrammeArrosageDTO> programmes = programmeService.findByParcelleId(parcelleId);
        return ResponseEntity.ok(programmes);
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Programmes par statut", description = "Récupère les programmes selon leur statut")
    public ResponseEntity<List<ProgrammeArrosageDTO>> getProgrammesByStatut(@PathVariable ProgrammeArrosage.StatutProgramme statut) {
        List<ProgrammeArrosageDTO> programmes = programmeService.findByStatut(statut);
        return ResponseEntity.ok(programmes);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un programme", description = "Met à jour un programme existant")
    public ResponseEntity<ProgrammeArrosageDTO> updateProgramme(
            @PathVariable Long id,
            @Valid @RequestBody ProgrammeArrosageDTO programmeDTO) {
        ProgrammeArrosageDTO updated = programmeService.update(id, programmeDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un programme", description = "Supprime un programme")
    public ResponseEntity<Void> deleteProgramme(@PathVariable Long id) {
        programmeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
