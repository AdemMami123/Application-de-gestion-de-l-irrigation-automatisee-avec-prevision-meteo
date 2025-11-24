package com.irrigation.meteo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * Custom metrics collector for Meteo Service
 * Tracks business-specific metrics for monitoring and observability
 */
@Component
@RequiredArgsConstructor
public class MeteoMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    
    // Counters for tracking operations
    private Counter previsionsFetchedCounter;
    private Counter weatherApiCallsCounter;
    private Counter weatherApiErrorsCounter;
    private Counter stationCreatedCounter;
    private Counter previsionCacheHitsCounter;
    private Counter previsionCacheMissesCounter;
    
    // Timers for measuring durations
    private Timer weatherApiFetchTimer;
    private Timer previsionProcessingTimer;
    
    @PostConstruct
    private void initializeMetrics() {
        // Initialize counters
        previsionsFetchedCounter = Counter.builder("meteo.previsions.fetched")
                .description("Total number of weather forecasts fetched")
                .tag("service", "meteo")
                .register(meterRegistry);
        
        weatherApiCallsCounter = Counter.builder("meteo.api.calls.total")
                .description("Total number of external weather API calls")
                .tag("service", "meteo")
                .register(meterRegistry);
        
        weatherApiErrorsCounter = Counter.builder("meteo.api.errors.total")
                .description("Total number of external weather API errors")
                .tag("service", "meteo")
                .register(meterRegistry);
        
        stationCreatedCounter = Counter.builder("meteo.stations.created")
                .description("Total number of weather stations created")
                .tag("service", "meteo")
                .register(meterRegistry);
        
        previsionCacheHitsCounter = Counter.builder("meteo.cache.hits")
                .description("Total number of forecast cache hits")
                .tag("service", "meteo")
                .register(meterRegistry);
        
        previsionCacheMissesCounter = Counter.builder("meteo.cache.misses")
                .description("Total number of forecast cache misses")
                .tag("service", "meteo")
                .register(meterRegistry);
        
        // Initialize timers
        weatherApiFetchTimer = Timer.builder("meteo.api.fetch.duration")
                .description("Time taken to fetch data from external weather API")
                .tag("service", "meteo")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
        
        previsionProcessingTimer = Timer.builder("meteo.prevision.processing.duration")
                .description("Time taken to process and store weather forecasts")
                .tag("service", "meteo")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }
    
    public void incrementPrevisionsFetched() {
        previsionsFetchedCounter.increment();
    }
    
    public void incrementWeatherApiCalls() {
        weatherApiCallsCounter.increment();
    }
    
    public void incrementWeatherApiErrors() {
        weatherApiErrorsCounter.increment();
    }
    
    public void incrementStationCreated() {
        stationCreatedCounter.increment();
    }
    
    public void incrementCacheHits() {
        previsionCacheHitsCounter.increment();
    }
    
    public void incrementCacheMisses() {
        previsionCacheMissesCounter.increment();
    }
    
    public Timer.Sample startWeatherApiFetchTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordWeatherApiFetchTime(Timer.Sample sample) {
        sample.stop(weatherApiFetchTimer);
    }
    
    public Timer.Sample startPrevisionProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordPrevisionProcessingTime(Timer.Sample sample) {
        sample.stop(previsionProcessingTimer);
    }
    
    /**
     * Record execution of a weather API call with timing
     * @param supplier Function that performs the API call
     * @param <T> Return type
     * @return The result from the supplier
     */
    public <T> T recordWeatherApiCall(java.util.function.Supplier<T> supplier) {
        incrementWeatherApiCalls();
        try {
            return weatherApiFetchTimer.recordCallable(supplier::get);
        } catch (Exception e) {
            incrementWeatherApiErrors();
            throw new RuntimeException(e);
        }
    }
}
