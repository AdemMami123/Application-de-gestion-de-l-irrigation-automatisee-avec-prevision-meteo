package com.irrigation.meteo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entité représentant une station météorologique
 */
@Entity
@Table(name = "station_meteo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationMeteo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la station est obligatoire")
    @Column(nullable = false, length = 100)
    private String nom;

    @NotNull(message = "La latitude est obligatoire")
    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @NotNull(message = "La longitude est obligatoire")
    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @NotBlank(message = "Le fournisseur est obligatoire")
    @Column(nullable = false, length = 100)
    private String fournisseur;
}
