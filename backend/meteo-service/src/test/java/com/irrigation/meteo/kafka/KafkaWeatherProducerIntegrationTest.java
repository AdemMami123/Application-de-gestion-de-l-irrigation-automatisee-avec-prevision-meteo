package com.irrigation.meteo.kafka;

import com.irrigation.meteo.event.WeatherChangeEvent;
import com.irrigation.meteo.event.WeatherChangeEvent.WeatherConditions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

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
class KafkaWeatherProducerIntegrationTest {

    @Autowired
    private KafkaWeatherProducer kafkaWeatherProducer;

    @Value("${app.kafka.topic.weather-change}")
    private String topic;

    private KafkaMessageListenerContainer<String, WeatherChangeEvent> container;
    private BlockingQueue<ConsumerRecord<String, WeatherChangeEvent>> records;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, WeatherChangeEvent.class);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, WeatherChangeEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(configs);

        ContainerProperties containerProperties = new ContainerProperties(topic);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, WeatherChangeEvent>) records::add);

        container.start();
        ContainerTestUtils.waitForAssignment(container, 1);
    }

    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    void testPublishWeatherChange() throws InterruptedException {
        // Given
        WeatherConditions oldConditions = new WeatherConditions(25.0, 15.0, 0.0, 5.0, LocalDateTime.now());
        WeatherConditions newConditions = new WeatherConditions(32.0, 20.0, 15.0, 12.0, LocalDateTime.now());

        WeatherChangeEvent event = new WeatherChangeEvent(
                1L,
                "Station Test",
                oldConditions,
                newConditions,
                LocalDateTime.now(),
                WeatherChangeEvent.ChangeSeverity.HIGH,
                "Hausse de température significative et augmentation des précipitations"
        );

        // When
        kafkaWeatherProducer.publishWeatherChangeSync(event);

        // Then
        ConsumerRecord<String, WeatherChangeEvent> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo("station-1");
        assertThat(received.value()).isNotNull();
        assertThat(received.value().getStationId()).isEqualTo(1L);
        assertThat(received.value().getStationNom()).isEqualTo("Station Test");
        assertThat(received.value().getSeverity()).isEqualTo(WeatherChangeEvent.ChangeSeverity.HIGH);
        assertThat(received.value().getNewConditions().getTemperatureMax()).isEqualTo(32.0);
    }

    @Test
    void testPublishCriticalWeatherChange() throws InterruptedException {
        // Given
        WeatherConditions oldConditions = new WeatherConditions(20.0, 12.0, 5.0, 10.0, LocalDateTime.now());
        WeatherConditions newConditions = new WeatherConditions(18.0, 10.0, 30.0, 35.0, LocalDateTime.now());

        WeatherChangeEvent event = new WeatherChangeEvent(
                2L,
                "Station Critique",
                oldConditions,
                newConditions,
                LocalDateTime.now(),
                WeatherChangeEvent.ChangeSeverity.CRITICAL,
                "Fortes précipitations et vent violent prévus"
        );

        // When
        kafkaWeatherProducer.publishWeatherChangeSync(event);

        // Then
        ConsumerRecord<String, WeatherChangeEvent> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.value().getSeverity()).isEqualTo(WeatherChangeEvent.ChangeSeverity.CRITICAL);
        assertThat(received.value().getNewConditions().getPluiePrevue()).isEqualTo(30.0);
        assertThat(received.value().getNewConditions().getVent()).isEqualTo(35.0);
    }
}
