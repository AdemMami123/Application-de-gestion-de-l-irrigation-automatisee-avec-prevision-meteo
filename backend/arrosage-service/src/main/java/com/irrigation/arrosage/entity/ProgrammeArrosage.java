package com.irrigation.arrosage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant un programme d'arrosage planifié
 */
@Entity
@Table(name = "programme_arrosage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgrammeArrosage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La parcelle est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcelle_id", nullable = false)
    private Parcelle parcelle;

    @NotNull(message = "La date planifiée est obligatoire")
    @Column(name = "date_planifiee", nullable = false)
    private LocalDateTime datePlanifiee;

    @NotNull(message = "La durée est obligatoire")
    @Positive(message = "La durée doit être positive")
    @Column(nullable = false)
    private Integer duree; // en minutes

    @NotNull(message = "Le volume prévu est obligatoire")
    @Positive(message = "Le volume prévu doit être positif")
    @Column(name = "volume_prevu", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumePrevu; // en m³

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutProgramme statut;

    public enum StatutProgramme {
        PLANIFIE,
        EN_COURS,
        TERMINE,
        ANNULE
    }
}
