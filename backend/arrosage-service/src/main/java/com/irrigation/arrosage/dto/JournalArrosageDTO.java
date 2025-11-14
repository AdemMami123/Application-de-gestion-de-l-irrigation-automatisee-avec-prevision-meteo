package com.irrigation.arrosage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalArrosageDTO {
    
    private Long id;
    
    @NotNull(message = "L'identifiant du programme est obligatoire")
    private Long programmeId;
    
    private String parcelleNom;
    
    @NotNull(message = "La date d'exécution est obligatoire")
    private LocalDateTime dateExecution;
    
    @NotNull(message = "Le volume réel est obligatoire")
    private BigDecimal volumeReel;
    
    private String remarque;
}
