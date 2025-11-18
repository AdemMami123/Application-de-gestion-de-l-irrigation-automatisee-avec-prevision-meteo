package com.irrigation.arrosage.kafka;

import com.irrigation.arrosage.event.WeatherChangeEvent;
import com.irrigation.arrosage.service.WeatherBasedSchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaWeatherConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaWeatherConsumer.class);

    private final WeatherBasedSchedulingService weatherBasedSchedulingService;

    public KafkaWeatherConsumer(WeatherBasedSchedulingService weatherBasedSchedulingService) {
        this.weatherBasedSchedulingService = weatherBasedSchedulingService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.weather-change}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeWeatherChangeEvent(
            @Payload WeatherChangeEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received weather change event from partition {} offset {} - Station: {}, Severity: {}", 
                    partition, offset, event.getStationId(), event.getSeverity());
            logger.debug("Event details: {}", event.getDescription());
            
            // Process the event based on severity
            processWeatherChange(event);
            
            // Manually acknowledge the message after successful processing
            acknowledgment.acknowledge();
            
            logger.info("Successfully processed weather change event for station {}", event.getStationId());
            
        } catch (Exception e) {
            logger.error("Error processing weather change event for station {}: {}", 
                    event.getStationId(), e.getMessage(), e);
            // Don't acknowledge - message will be reprocessed or sent to DLQ
            throw e;
        }
    }

    private void processWeatherChange(WeatherChangeEvent event) {
        switch (event.getSeverity()) {
            case CRITICAL:
                logger.warn("CRITICAL weather change detected for station {} - Immediate action required", 
                        event.getStationId());
                weatherBasedSchedulingService.handleCriticalWeatherChange(event);
                break;
                
            case HIGH:
                logger.info("HIGH severity weather change for station {} - Adjusting schedules", 
                        event.getStationId());
                weatherBasedSchedulingService.handleHighSeverityWeatherChange(event);
                break;
                
            case MEDIUM:
                logger.info("MEDIUM severity weather change for station {} - Reviewing schedules", 
                        event.getStationId());
                weatherBasedSchedulingService.handleMediumSeverityWeatherChange(event);
                break;
                
            case LOW:
            default:
                logger.debug("LOW severity weather change for station {} - No action needed", 
                        event.getStationId());
                break;
        }
    }
}
