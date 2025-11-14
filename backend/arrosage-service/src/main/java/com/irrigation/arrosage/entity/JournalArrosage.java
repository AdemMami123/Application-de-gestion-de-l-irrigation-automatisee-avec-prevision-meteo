package com.irrigation.arrosage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant un journal d'exécution d'arrosage
 */
@Entity
@Table(name = "journal_arrosage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalArrosage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le programme d'arrosage est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id", nullable = false)
    private ProgrammeArrosage programme;

    @NotNull(message = "La date d'exécution est obligatoire")
    @Column(name = "date_execution", nullable = false)
    private LocalDateTime dateExecution;

    @NotNull(message = "Le volume réel est obligatoire")
    @Column(name = "volume_reel", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumeReel; // en m³

    @Column(length = 500)
    private String remarque;
}
