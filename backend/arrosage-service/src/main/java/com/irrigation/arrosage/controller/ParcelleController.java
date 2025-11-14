package com.irrigation.arrosage.controller;

import com.irrigation.arrosage.dto.ParcelleDTO;
import com.irrigation.arrosage.service.ParcelleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parcelles")
@RequiredArgsConstructor
@Tag(name = "Parcelles", description = "API de gestion des parcelles agricoles")
public class ParcelleController {

    private final ParcelleService parcelleService;

    @PostMapping
    @Operation(summary = "Créer une parcelle", description = "Crée une nouvelle parcelle agricole")
    public ResponseEntity<ParcelleDTO> createParcelle(@Valid @RequestBody ParcelleDTO parcelleDTO) {
        ParcelleDTO created = parcelleService.create(parcelleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les parcelles", description = "Récupère toutes les parcelles")
    public ResponseEntity<List<ParcelleDTO>> getAllParcelles() {
        List<ParcelleDTO> parcelles = parcelleService.findAll();
        return ResponseEntity.ok(parcelles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une parcelle", description = "Récupère une parcelle par son ID")
    public ResponseEntity<ParcelleDTO> getParcelleById(@PathVariable Long id) {
        ParcelleDTO parcelle = parcelleService.findById(id);
        return ResponseEntity.ok(parcelle);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une parcelle", description = "Met à jour une parcelle existante")
    public ResponseEntity<ParcelleDTO> updateParcelle(
            @PathVariable Long id,
            @Valid @RequestBody ParcelleDTO parcelleDTO) {
        ParcelleDTO updated = parcelleService.update(id, parcelleDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une parcelle", description = "Supprime une parcelle")
    public ResponseEntity<Void> deleteParcelle(@PathVariable Long id) {
        parcelleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/culture/{culture}")
    @Operation(summary = "Rechercher par culture", description = "Récupère toutes les parcelles d'une culture spécifique")
    public ResponseEntity<List<ParcelleDTO>> getParcellesByCulture(@PathVariable String culture) {
        List<ParcelleDTO> parcelles = parcelleService.findByCulture(culture);
        return ResponseEntity.ok(parcelles);
    }
}
