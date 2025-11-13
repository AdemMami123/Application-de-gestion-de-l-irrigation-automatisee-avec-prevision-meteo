package com.irrigation.meteo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour StationMeteo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationMeteoDTO {
    
    private Long id;
    
    @NotBlank(message = "Le nom de la station est obligatoire")
    private String nom;
    
    @NotNull(message = "La latitude est obligatoire")
    private BigDecimal latitude;
    
    @NotNull(message = "La longitude est obligatoire")
    private BigDecimal longitude;
    
    @NotBlank(message = "Le fournisseur est obligatoire")
    private String fournisseur;
}
