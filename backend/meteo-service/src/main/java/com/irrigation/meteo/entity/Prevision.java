package com.irrigation.meteo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entité représentant une prévision météorologique
 */
@Entity
@Table(name = "prevision")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La station météo est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private StationMeteo station;

    @NotNull(message = "La date est obligatoire")
    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "temperature_max", precision = 5, scale = 2)
    private BigDecimal temperatureMax;

    @Column(name = "temperature_min", precision = 5, scale = 2)
    private BigDecimal temperatureMin;

    @Column(name = "pluie_prevue", precision = 6, scale = 2)
    private BigDecimal pluiePrevue;

    @Column(precision = 5, scale = 2)
    private BigDecimal vent;
}
