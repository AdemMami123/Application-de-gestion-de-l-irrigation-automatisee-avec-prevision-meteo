package com.irrigation.arrosage.controller;

import com.irrigation.arrosage.dto.JournalArrosageDTO;
import com.irrigation.arrosage.service.JournalArrosageService;
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
@RequestMapping("/api/journaux")
@RequiredArgsConstructor
@Tag(name = "Journaux d'Arrosage", description = "API de gestion des journaux d'exécution d'arrosage")
public class JournalArrosageController {

    private final JournalArrosageService journalService;

    @PostMapping
    @Operation(summary = "Créer un journal", description = "Crée un nouveau journal d'exécution d'arrosage")
    public ResponseEntity<JournalArrosageDTO> createJournal(@Valid @RequestBody JournalArrosageDTO journalDTO) {
        JournalArrosageDTO created = journalService.create(journalDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister tous les journaux", description = "Récupère tous les journaux d'arrosage")
    public ResponseEntity<List<JournalArrosageDTO>> getAllJournaux() {
        List<JournalArrosageDTO> journaux = journalService.findAll();
        return ResponseEntity.ok(journaux);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un journal", description = "Récupère un journal par son ID")
    public ResponseEntity<JournalArrosageDTO> getJournalById(@PathVariable Long id) {
        JournalArrosageDTO journal = journalService.findById(id);
        return ResponseEntity.ok(journal);
    }

    @GetMapping("/programme/{programmeId}")
    @Operation(summary = "Journaux par programme", description = "Récupère tous les journaux d'un programme")
    public ResponseEntity<List<JournalArrosageDTO>> getJournauxByProgramme(@PathVariable Long programmeId) {
        List<JournalArrosageDTO> journaux = journalService.findByProgrammeId(programmeId);
        return ResponseEntity.ok(journaux);
    }

    @GetMapping("/parcelle/{parcelleId}")
    @Operation(summary = "Journaux par parcelle", description = "Récupère tous les journaux d'une parcelle")
    public ResponseEntity<List<JournalArrosageDTO>> getJournauxByParcelle(@PathVariable Long parcelleId) {
        List<JournalArrosageDTO> journaux = journalService.findByParcelleId(parcelleId);
        return ResponseEntity.ok(journaux);
    }

    @GetMapping("/periode")
    @Operation(summary = "Journaux par période", description = "Récupère les journaux entre deux dates")
    public ResponseEntity<List<JournalArrosageDTO>> getJournauxByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<JournalArrosageDTO> journaux = journalService.findByPeriode(startDate, endDate);
        return ResponseEntity.ok(journaux);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un journal", description = "Met à jour un journal existant")
    public ResponseEntity<JournalArrosageDTO> updateJournal(
            @PathVariable Long id,
            @Valid @RequestBody JournalArrosageDTO journalDTO) {
        JournalArrosageDTO updated = journalService.update(id, journalDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un journal", description = "Supprime un journal")
    public ResponseEntity<Void> deleteJournal(@PathVariable Long id) {
        journalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
