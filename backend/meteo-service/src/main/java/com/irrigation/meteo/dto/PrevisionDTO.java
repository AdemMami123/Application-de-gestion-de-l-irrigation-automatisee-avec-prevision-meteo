package com.irrigation.meteo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour Prevision
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrevisionDTO {
    
    private Long id;
    
    @NotNull(message = "L'identifiant de la station est obligatoire")
    private Long stationId;
    
    private String stationNom;
    
    @NotNull(message = "La date est obligatoire")
    private LocalDate date;
    
    private BigDecimal temperatureMax;
    
    private BigDecimal temperatureMin;
    
    private BigDecimal pluiePrevue;
    
    private BigDecimal vent;
}
