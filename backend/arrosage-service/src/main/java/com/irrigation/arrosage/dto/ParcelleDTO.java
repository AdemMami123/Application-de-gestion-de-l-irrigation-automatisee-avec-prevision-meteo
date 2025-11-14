package com.irrigation.arrosage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelleDTO {
    
    private Long id;
    
    @NotBlank(message = "Le nom de la parcelle est obligatoire")
    private String nom;
    
    @NotNull(message = "La superficie est obligatoire")
    @Positive(message = "La superficie doit être positive")
    private BigDecimal superficie;
    
    @NotBlank(message = "Le type de culture est obligatoire")
    private String culture;
}
