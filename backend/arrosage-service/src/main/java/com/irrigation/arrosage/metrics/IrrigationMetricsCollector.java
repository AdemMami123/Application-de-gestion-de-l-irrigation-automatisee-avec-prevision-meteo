package com.irrigation.arrosage.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom metrics collector for Arrosage Service
 * Tracks business-specific metrics for irrigation management and monitoring
 */
@Component
@RequiredArgsConstructor
public class IrrigationMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    
    // Counters for tracking operations
    private Counter irrigationProgramsExecutedCounter;
    private Counter irrigationProgramsCreatedCounter;
    private Counter parcellesCreatedCounter;
    private Counter sensorsReadCounter;
    private Counter alertsGeneratedCounter;
    private Counter waterUsageCounter;
    
    // Timers for measuring durations
    private Timer irrigationExecutionTimer;
    private Timer sensorReadTimer;
    
    // Gauges for current state
    private AtomicInteger activeIrrigationPrograms;
    private AtomicInteger totalParcelles;
    
    @PostConstruct
    private void initializeMetrics() {
        // Initialize counters
        irrigationProgramsExecutedCounter = Counter.builder("irrigation.programs.executed")
                .description("Total number of irrigation programs executed")
                .tag("service", "arrosage")
                .register(meterRegistry);
        
        irrigationProgramsCreatedCounter = Counter.builder("irrigation.programs.created")
                .description("Total number of irrigation programs created")
                .tag("service", "arrosage")
                .register(meterRegistry);
        
        parcellesCreatedCounter = Counter.builder("irrigation.parcelles.created")
                .description("Total number of parcelles (plots) created")
                .tag("service", "arrosage")
                .register(meterRegistry);
        
        sensorsReadCounter = Counter.builder("irrigation.sensors.read")
                .description("Total number of sensor readings")
                .tag("service", "arrosage")
                .register(meterRegistry);
        
        alertsGeneratedCounter = Counter.builder("irrigation.alerts.generated")
                .description("Total number of irrigation alerts generated")
                .tag("service", "arrosage")
                .register(meterRegistry);
        
        waterUsageCounter = Counter.builder("irrigation.water.usage.cubic.meters")
                .description("Total water usage in cubic meters")
                .tag("service", "arrosage")
                .register(meterRegistry);
        
        // Initialize timers
        irrigationExecutionTimer = Timer.builder("irrigation.execution.duration")
                .description("Time taken to execute an irrigation program")
                .tag("service", "arrosage")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
        
        sensorReadTimer = Timer.builder("irrigation.sensor.read.duration")
                .description("Time taken to read sensor data")
                .tag("service", "arrosage")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
        
        // Initialize gauges
        activeIrrigationPrograms = new AtomicInteger(0);
        Gauge.builder("irrigation.programs.active", 
                activeIrrigationPrograms,
                AtomicInteger::get)
                .tag("service", "arrosage")
                .register(meterRegistry);
        
        totalParcelles = new AtomicInteger(0);
        Gauge.builder("irrigation.parcelles.total",
                totalParcelles,
                AtomicInteger::get)
                .tag("service", "arrosage")
                .register(meterRegistry);
    }
    
    public void incrementIrrigationProgramsExecuted() {
        irrigationProgramsExecutedCounter.increment();
    }
    
    public void incrementIrrigationProgramsCreated() {
        irrigationProgramsCreatedCounter.increment();
    }
    
    public void incrementParcellesCreated() {
        parcellesCreatedCounter.increment();
    }
    
    public void incrementSensorsRead() {
        sensorsReadCounter.increment();
    }
    
    public void incrementAlertsGenerated() {
        alertsGeneratedCounter.increment();
    }
    
    public void recordWaterUsage(double cubicMeters) {
        waterUsageCounter.increment(cubicMeters);
    }
    
    public void setActiveIrrigationPrograms(int count) {
        activeIrrigationPrograms.set(count);
    }
    
    public void setTotalParcelles(int count) {
        totalParcelles.set(count);
    }
    
    public Timer.Sample startIrrigationExecutionTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordIrrigationExecutionTime(Timer.Sample sample) {
        sample.stop(irrigationExecutionTimer);
    }
    
    public Timer.Sample startSensorReadTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordSensorReadTime(Timer.Sample sample) {
        sample.stop(sensorReadTimer);
    }
    
    /**
     * Record execution of an irrigation program with timing
     * @param supplier Function that executes the irrigation program
     * @param <T> Return type
     * @return The result from the supplier
     */
    public <T> T recordIrrigationExecution(java.util.function.Supplier<T> supplier) {
        try {
            return irrigationExecutionTimer.recordCallable(supplier::get);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
