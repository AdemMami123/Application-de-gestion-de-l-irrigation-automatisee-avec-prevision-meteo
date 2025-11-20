package com.irrigation.arrosage.config;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import redis.embedded.RedisServer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour la configuration du cache Redis avec embedded Redis
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6370"  // Port différent pour éviter les conflits
})
public class RedisCacheConfigTest {

    private static RedisServer redisServer;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeAll
    static void startRedis() throws IOException {
        redisServer = new RedisServer(6370);
        redisServer.start();
    }

    @AfterAll
    static void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        // Nettoyer Redis avant chaque test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testCacheManagerIsConfigured() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrder(
                "forecasts", "stations", "weather-data"
        );
    }

    @Test
    void testForecastsCacheExists() {
        var forecastsCache = cacheManager.getCache("forecasts");
        assertThat(forecastsCache).isNotNull();
    }

    @Test
    void testStationsCacheExists() {
        var stationsCache = cacheManager.getCache("stations");
        assertThat(stationsCache).isNotNull();
    }

    @Test
    void testWeatherDataCacheExists() {
        var weatherCache = cacheManager.getCache("weather-data");
        assertThat(weatherCache).isNotNull();
    }

    @Test
    void testCachePutAndGet() {
        // Arrange
        var forecastsCache = cacheManager.getCache("forecasts");
        String key = "test-key";
        String value = "test-value";

        // Act
        forecastsCache.put(key, value);
        var cachedValue = forecastsCache.get(key, String.class);

        // Assert
        assertThat(cachedValue).isEqualTo(value);
    }

    @Test
    void testCacheEvict() {
        // Arrange
        var forecastsCache = cacheManager.getCache("forecasts");
        String key = "test-key";
        String value = "test-value";
        forecastsCache.put(key, value);

        // Act
        forecastsCache.evict(key);
        var cachedValue = forecastsCache.get(key);

        // Assert
        assertThat(cachedValue).isNull();
    }

    @Test
    void testCacheClear() {
        // Arrange
        var forecastsCache = cacheManager.getCache("forecasts");
        forecastsCache.put("key1", "value1");
        forecastsCache.put("key2", "value2");

        // Act
        forecastsCache.clear();

        // Assert
        assertThat(forecastsCache.get("key1")).isNull();
        assertThat(forecastsCache.get("key2")).isNull();
    }

    @Test
    void testRedisTemplateIsConfigured() {
        assertThat(redisTemplate).isNotNull();
        assertThat(redisTemplate.getConnectionFactory()).isNotNull();
    }

    @Test
    void testRedisConnection() {
        // Test que nous pouvons nous connecter à Redis
        String ping = redisTemplate.getConnectionFactory().getConnection().ping();
        assertThat(ping).isEqualTo("PONG");
    }

    @Test
    void testRedisTemplateOperations() {
        // Arrange
        String key = "test:key";
        String value = "test-value";

        // Act
        redisTemplate.opsForValue().set(key, value);
        Object retrieved = redisTemplate.opsForValue().get(key);

        // Assert
        assertThat(retrieved).isEqualTo(value);
    }

    @Test
    void testCacheKeyPrefix() {
        // Arrange
        var forecastsCache = cacheManager.getCache("forecasts");
        String key = "station:123";
        String value = "forecast-data";

        // Act
        forecastsCache.put(key, value);
        
        // Vérifier que la clé est préfixée dans Redis
        var keys = redisTemplate.keys("arrosage:forecasts::*");

        // Assert
        assertThat(keys).isNotNull();
        assertThat(keys).isNotEmpty();
    }
}
