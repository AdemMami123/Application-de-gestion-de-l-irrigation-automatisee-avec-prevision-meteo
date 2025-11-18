package com.irrigation.arrosage.kafka;

import com.irrigation.arrosage.entity.Parcelle;
import com.irrigation.arrosage.entity.ProgrammeArrosage;
import com.irrigation.arrosage.entity.StatutProgramme;
import com.irrigation.arrosage.event.WeatherChangeEvent;
import com.irrigation.arrosage.event.WeatherChangeEvent.WeatherConditions;
import com.irrigation.arrosage.repository.ParcelleRepository;
import com.irrigation.arrosage.repository.ProgrammeArrosageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9093",
                "port=9093"
        },
        topics = {"weather-change-events"}
)
@DirtiesContext
class KafkaWeatherConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ProgrammeArrosageRepository programmeRepository;

    @Autowired
    private ParcelleRepository parcelleRepository;

    @Value("${app.kafka.topic.weather-change}")
    private String topic;

    private Parcelle testParcelle;

    @BeforeEach
    void setUp() {
        // Clean up
        programmeRepository.deleteAll();
        parcelleRepository.deleteAll();

        // Create test parcelle
        testParcelle = new Parcelle();
        testParcelle.setNom("Parcelle Test Kafka");
        testParcelle.setSuperficie(5000.0);
        testParcelle.setCulture("Tomates");
        testParcelle = parcelleRepository.save(testParcelle);
    }

    @Test
    void testConsumeHighSeverityWeatherChange() {
        // Given - Create a programme scheduled for tomorrow
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        ProgrammeArrosage programme = createProgramme(tomorrow, 60, 25.0);

        // Weather change event with significant rain increase
        WeatherConditions oldConditions = new WeatherConditions(25.0, 15.0, 0.0, 5.0, tomorrow);
        WeatherConditions newConditions = new WeatherConditions(24.0, 16.0, 15.0, 8.0, tomorrow);

        WeatherChangeEvent event = new WeatherChangeEvent(
                1L,
                "Station Test",
                oldConditions,
                newConditions,
                LocalDateTime.now(),
                WeatherChangeEvent.ChangeSeverity.HIGH,
                "Augmentation des précipitations prévues"
        );

        // When - Publish event
        kafkaTemplate.send(topic, "station-1", event);

        // Then - Wait for consumer to process and verify volume was reduced
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ProgrammeArrosage updated = programmeRepository.findById(programme.getId()).orElseThrow();
            assertThat(updated.getVolumePrevu()).isLessThan(25.0);
        });
    }

    @Test
    void testConsumeCriticalWeatherChange_HeavyRain() {
        // Given - Create a programme
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        ProgrammeArrosage programme = createProgramme(tomorrow, 90, 35.0);

        // Critical weather change with heavy rain
        WeatherConditions oldConditions = new WeatherConditions(22.0, 12.0, 5.0, 10.0, tomorrow);
        WeatherConditions newConditions = new WeatherConditions(20.0, 10.0, 25.0, 12.0, tomorrow);

        WeatherChangeEvent event = new WeatherChangeEvent(
                1L,
                "Station Test",
                oldConditions,
                newConditions,
                LocalDateTime.now(),
                WeatherChangeEvent.ChangeSeverity.CRITICAL,
                "Fortes précipitations prévues"
        );

        // When
        kafkaTemplate.send(topic, "station-1", event);

        // Then - Programme should be cancelled
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ProgrammeArrosage updated = programmeRepository.findById(programme.getId()).orElseThrow();
            assertThat(updated.getStatut()).isEqualTo(StatutProgramme.ANNULE);
        });
    }

    @Test
    void testConsumeCriticalWeatherChange_HighWind() {
        // Given
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        ProgrammeArrosage programme = createProgramme(tomorrow, 60, 30.0);
        int originalDuration = programme.getDuree();

        // Critical weather with high wind
        WeatherConditions oldConditions = new WeatherConditions(28.0, 18.0, 2.0, 8.0, tomorrow);
        WeatherConditions newConditions = new WeatherConditions(30.0, 20.0, 3.0, 35.0, tomorrow);

        WeatherChangeEvent event = new WeatherChangeEvent(
                1L,
                "Station Test",
                oldConditions,
                newConditions,
                LocalDateTime.now(),
                WeatherChangeEvent.ChangeSeverity.CRITICAL,
                "Vent violent prévu"
        );

        // When
        kafkaTemplate.send(topic, "station-1", event);

        // Then - Duration should be increased due to high wind
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ProgrammeArrosage updated = programmeRepository.findById(programme.getId()).orElseThrow();
            assertThat(updated.getDuree()).isGreaterThan(originalDuration);
        });
    }

    @Test
    void testConsumeMediumSeverityWeatherChange() {
        // Given
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(6).withMinute(0);
        ProgrammeArrosage programme = createProgramme(tomorrow, 60, 20.0);
        double originalVolume = programme.getVolumePrevu();

        // Medium severity change
        WeatherConditions oldConditions = new WeatherConditions(23.0, 13.0, 0.0, 5.0, tomorrow);
        WeatherConditions newConditions = new WeatherConditions(24.0, 14.0, 7.0, 6.0, tomorrow);

        WeatherChangeEvent event = new WeatherChangeEvent(
                1L,
                "Station Test",
                oldConditions,
                newConditions,
                LocalDateTime.now(),
                WeatherChangeEvent.ChangeSeverity.MEDIUM,
                "Légère augmentation des précipitations"
        );

        // When
        kafkaTemplate.send(topic, "station-1", event);

        // Then - Minor volume adjustment
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ProgrammeArrosage updated = programmeRepository.findById(programme.getId()).orElseThrow();
            assertThat(updated.getVolumePrevu()).isLessThan(originalVolume);
            assertThat(updated.getStatut()).isEqualTo(StatutProgramme.PLANIFIE);
        });
    }

    private ProgrammeArrosage createProgramme(LocalDateTime datePlanifiee, int duree, double volume) {
        ProgrammeArrosage programme = new ProgrammeArrosage();
        programme.setParcelle(testParcelle);
        programme.setDatePlanifiee(datePlanifiee);
        programme.setDuree(duree);
        programme.setVolumePrevu(volume);
        programme.setStatut(StatutProgramme.PLANIFIE);
        return programmeRepository.save(programme);
    }
}
