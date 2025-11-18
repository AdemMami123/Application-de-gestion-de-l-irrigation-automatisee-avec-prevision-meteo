package com.irrigation.meteo.event;

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
        
        public double getTemperatureDifference(WeatherConditions other) {
            if (this.temperatureMax == null || other.temperatureMax == null) {
                return 0.0;
            }
            return Math.abs(this.temperatureMax - other.temperatureMax);
        }
        
        public double getRainDifference(WeatherConditions other) {
            if (this.pluiePrevue == null || other.pluiePrevue == null) {
                return 0.0;
            }
            return Math.abs(this.pluiePrevue - other.pluiePrevue);
        }
        
        public double getWindSpeedDifference(WeatherConditions other) {
            if (this.vent == null || other.vent == null) {
                return 0.0;
            }
            return Math.abs(this.vent - other.vent);
        }
    }
    
    public enum ChangeSeverity {
        LOW,      // Minor changes, no immediate action needed
        MEDIUM,   // Moderate changes, consider adjusting schedules
        HIGH,     // Significant changes, immediate schedule adjustment recommended
        CRITICAL  // Extreme changes, urgent action required
    }
    
    public static ChangeSeverity calculateSeverity(WeatherConditions oldCond, WeatherConditions newCond) {
        if (oldCond == null || newCond == null) {
            return ChangeSeverity.LOW;
        }
        
        double tempDiff = oldCond.getTemperatureDifference(newCond);
        double rainDiff = oldCond.getRainDifference(newCond);
        double windDiff = oldCond.getWindSpeedDifference(newCond);
        
        // Check for critical conditions
        if (rainDiff > 20 || tempDiff > 10 || windDiff > 20) {
            return ChangeSeverity.CRITICAL;
        }
        
        // Check for high severity
        if (rainDiff > 10 || tempDiff > 5 || windDiff > 10) {
            return ChangeSeverity.HIGH;
        }
        
        // Check for medium severity
        if (rainDiff > 5 || tempDiff > 3 || windDiff > 5) {
            return ChangeSeverity.MEDIUM;
        }
        
        return ChangeSeverity.LOW;
    }
    
    public static String generateDescription(WeatherConditions oldCond, WeatherConditions newCond) {
        StringBuilder desc = new StringBuilder();
        
        if (oldCond == null || newCond == null) {
            return "Nouvelles conditions météo disponibles";
        }
        
        double tempDiff = oldCond.getTemperatureDifference(newCond);
        if (tempDiff > 3) {
            if (newCond.getTemperatureMax() != null && oldCond.getTemperatureMax() != null) {
                if (newCond.getTemperatureMax() > oldCond.getTemperatureMax()) {
                    desc.append(String.format("Hausse de température de %.1f°C. ", tempDiff));
                } else {
                    desc.append(String.format("Baisse de température de %.1f°C. ", tempDiff));
                }
            }
        }
        
        double rainDiff = oldCond.getRainDifference(newCond);
        if (rainDiff > 5) {
            if (newCond.getPluiePrevue() != null && oldCond.getPluiePrevue() != null) {
                if (newCond.getPluiePrevue() > oldCond.getPluiePrevue()) {
                    desc.append(String.format("Augmentation des précipitations prévues de %.1f mm. ", rainDiff));
                } else {
                    desc.append(String.format("Diminution des précipitations prévues de %.1f mm. ", rainDiff));
                }
            }
        }
        
        double windDiff = oldCond.getWindSpeedDifference(newCond);
        if (windDiff > 5) {
            desc.append(String.format("Changement de vent: %.1f km/h. ", windDiff));
        }
        
        if (desc.length() == 0) {
            return "Mise à jour des conditions météo";
        }
        
        return desc.toString().trim();
    }
}
