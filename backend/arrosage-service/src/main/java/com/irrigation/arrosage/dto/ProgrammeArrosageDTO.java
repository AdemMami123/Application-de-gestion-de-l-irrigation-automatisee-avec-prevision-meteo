package com.irrigation.arrosage.dto;

import com.irrigation.arrosage.entity.ProgrammeArrosage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class ProgrammeArrosageDTO {
    
    private Long id;
    
    @NotNull(message = "L'identifiant de la parcelle est obligatoire")
    private Long parcelleId;
    
    private String parcelleNom;
    
    @NotNull(message = "La date planifiée est obligatoire")
    private LocalDateTime datePlanifiee;
    
    @NotNull(message = "La durée est obligatoire")
    @Positive(message = "La durée doit être positive")
    private Integer duree;
    
    @NotNull(message = "Le volume prévu est obligatoire")
    @Positive(message = "Le volume prévu doit être positif")
    private BigDecimal volumePrevu;
    
    @NotNull(message = "Le statut est obligatoire")
    private ProgrammeArrosage.StatutProgramme statut;
}
