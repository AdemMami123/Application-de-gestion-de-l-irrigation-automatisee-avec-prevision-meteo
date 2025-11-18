package com.irrigation.arrosage.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherChangeEvent {
    
    private Long stationId;
    private String stationNom;
    private WeatherConditions oldConditions;
    private WeatherConditions newConditions;
    private LocalDateTime timestamp;
    private ChangeSeverity severity;
    private String description;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherConditions {
        private Double temperatureMax;
        private Double temperatureMin;
        private Double pluiePrevue;
        private Double vent;
        private LocalDateTime date;
    }
    
    public enum ChangeSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
