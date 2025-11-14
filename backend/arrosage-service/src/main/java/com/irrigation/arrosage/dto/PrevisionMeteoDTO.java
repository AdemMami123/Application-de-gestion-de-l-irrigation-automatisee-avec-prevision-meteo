package com.irrigation.arrosage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour récupérer les prévisions depuis le service météo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrevisionMeteoDTO {
    
    private Long id;
    private Long stationId;
    private String stationNom;
    private LocalDate date;
    private BigDecimal temperatureMax;
    private BigDecimal temperatureMin;
    private BigDecimal pluiePrevue;
    private BigDecimal vent;
}
