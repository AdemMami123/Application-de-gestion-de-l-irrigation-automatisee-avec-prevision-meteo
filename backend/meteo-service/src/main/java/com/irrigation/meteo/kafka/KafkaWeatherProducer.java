package com.irrigation.meteo.kafka;

import com.irrigation.meteo.event.WeatherChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaWeatherProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaWeatherProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String weatherChangeTopic;

    public KafkaWeatherProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topic.weather-change}") String weatherChangeTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.weatherChangeTopic = weatherChangeTopic;
    }

    public void publishWeatherChange(WeatherChangeEvent event) {
        try {
            String key = "station-" + event.getStationId();
            
            logger.info("Publishing weather change event for station {} with severity {}", 
                    event.getStationId(), event.getSeverity());
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(weatherChangeTopic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully published weather change event for station {} to topic {} - Offset: {}", 
                            event.getStationId(), 
                            weatherChangeTopic,
                            result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish weather change event for station {}: {}", 
                            event.getStationId(), 
                            ex.getMessage());
                }
            });
            
        } catch (Exception e) {
            logger.error("Error publishing weather change event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish weather change event", e);
        }
    }
    
    public void publishWeatherChangeSync(WeatherChangeEvent event) {
        try {
            String key = "station-" + event.getStationId();
            
            logger.info("Publishing weather change event synchronously for station {}", event.getStationId());
            
            SendResult<String, Object> result = kafkaTemplate.send(weatherChangeTopic, key, event).get();
            
            logger.info("Successfully published weather change event for station {} - Offset: {}", 
                    event.getStationId(), 
                    result.getRecordMetadata().offset());
            
        } catch (Exception e) {
            logger.error("Error publishing weather change event synchronously: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish weather change event", e);
        }
    }
}
