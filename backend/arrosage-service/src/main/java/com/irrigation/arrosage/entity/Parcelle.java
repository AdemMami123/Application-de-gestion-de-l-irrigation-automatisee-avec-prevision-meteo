package com.irrigation.arrosage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entité représentant une parcelle agricole
 */
@Entity
@Table(name = "parcelle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parcelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la parcelle est obligatoire")
    @Column(nullable = false, length = 100)
    private String nom;

    @NotNull(message = "La superficie est obligatoire")
    @Positive(message = "La superficie doit être positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal superficie;

    @NotBlank(message = "Le type de culture est obligatoire")
    @Column(nullable = false, length = 100)
    private String culture;
}
