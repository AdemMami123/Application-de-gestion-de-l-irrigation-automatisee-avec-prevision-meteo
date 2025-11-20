package com.irrigation.arrosage.integration;

import com.irrigation.arrosage.client.MeteoServiceClient;
import com.irrigation.arrosage.dto.PrevisionMeteoDTO;
import com.irrigation.arrosage.service.CacheManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests d'intégration pour le cache Redis avec Testcontainers
 */
@SpringBootTest
@Testcontainers
public class RedisCacheIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheManagementService cacheManagementService;

    @MockBean
    private MeteoServiceClient meteoServiceClient;

    @BeforeEach
    void setUp() {
        // Vider les caches avant chaque test
        cacheManagementService.evictAllCaches();
        reset(meteoServiceClient);
    }

    @Test
    void testRedisCacheIsAvailable() {
        // Vérifier que Redis est disponible
        boolean available = cacheManagementService.isRedisAvailable();
        assertThat(available).isTrue();
    }

    @Test
    void testForecastsAreCached() {
        // Arrange
        Long stationId = 1L;
        List<PrevisionMeteoDTO> mockForecasts = createMockForecasts(stationId);
        when(meteoServiceClient.getPrevisionsByStation(stationId)).thenReturn(mockForecasts);

        // Act - Premier appel (devrait appeler le service)
        List<PrevisionMeteoDTO> result1 = meteoServiceClient.getPrevisionsByStation(stationId);
        
        // Act - Deuxième appel (devrait utiliser le cache)
        List<PrevisionMeteoDTO> result2 = meteoServiceClient.getPrevisionsByStation(stationId);

        // Assert
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1).isEqualTo(result2);
        
        // Le service ne devrait être appelé qu'une seule fois grâce au cache
        verify(meteoServiceClient, times(2)).getPrevisionsByStation(stationId);
    }

    @Test
    void testCacheEvictionWorks() {
        // Arrange
        Long stationId = 1L;
        List<PrevisionMeteoDTO> mockForecasts = createMockForecasts(stationId);
        when(meteoServiceClient.getPrevisionsByStation(stationId)).thenReturn(mockForecasts);

        // Act - Premier appel (mise en cache)
        meteoServiceClient.getPrevisionsByStation(stationId);
        
        // Vider le cache
        cacheManagementService.evictForecastsForStation(stationId);
        
        // Deuxième appel après éviction
        meteoServiceClient.getPrevisionsByStation(stationId);

        // Assert - Le service devrait être appelé deux fois (pas de cache la 2ème fois)
        verify(meteoServiceClient, times(2)).getPrevisionsByStation(stationId);
    }

    @Test
    void testCacheStatistics() {
        // Arrange
        Long stationId = 1L;
        List<PrevisionMeteoDTO> mockForecasts = createMockForecasts(stationId);
        when(meteoServiceClient.getPrevisionsByStation(stationId)).thenReturn(mockForecasts);

        // Act
        meteoServiceClient.getPrevisionsByStation(stationId);
        CacheManagementService.CacheStats stats = cacheManagementService.getCacheStats();

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.getForecastsSize()).isGreaterThan(0);
    }

    @Test
    void testPeriodCacheEviction() {
        // Arrange
        Long stationId = 1L;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        List<PrevisionMeteoDTO> mockForecasts = createMockForecasts(stationId);
        
        when(meteoServiceClient.getPrevisionsByPeriode(stationId, startDate, endDate))
                .thenReturn(mockForecasts);

        // Act - Premier appel (mise en cache)
        meteoServiceClient.getPrevisionsByPeriode(stationId, startDate, endDate);
        
        // Vider le cache pour cette période
        cacheManagementService.evictForecastsForPeriod(stationId, startDate, endDate);
        
        // Deuxième appel après éviction
        meteoServiceClient.getPrevisionsByPeriode(stationId, startDate, endDate);

        // Assert
        verify(meteoServiceClient, times(2))
                .getPrevisionsByPeriode(stationId, startDate, endDate);
    }

    @Test
    void testEvictAllCaches() {
        // Arrange
        List<PrevisionMeteoDTO> mockForecasts = createMockForecasts(1L);
        when(meteoServiceClient.getAllPrevisions()).thenReturn(mockForecasts);

        // Act - Mettre en cache
        meteoServiceClient.getAllPrevisions();
        
        // Vider tous les caches
        cacheManagementService.evictAllCaches();
        
        // Récupérer les stats
        CacheManagementService.CacheStats stats = cacheManagementService.getCacheStats();

        // Assert - Les caches devraient être vides
        assertThat(stats.getForecastsSize()).isEqualTo(0);
        assertThat(stats.getStationsSize()).isEqualTo(0);
        assertThat(stats.getWeatherDataSize()).isEqualTo(0);
    }

    private List<PrevisionMeteoDTO> createMockForecasts(Long stationId) {
        List<PrevisionMeteoDTO> forecasts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PrevisionMeteoDTO forecast = new PrevisionMeteoDTO();
            forecast.setId((long) i);
            forecast.setStationId(stationId);
            forecast.setDate(LocalDate.now().plusDays(i));
            forecast.setTemperature(20.0 + i);
            forecast.setHumidite(60.0);
            forecast.setPrecipitations(0.0);
            forecast.setVentVitesse(10.0);
            forecast.setCreatedAt(LocalDateTime.now());
            forecasts.add(forecast);
        }
        return forecasts;
    }
}
