package com.irrigation.arrosage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Service de gestion du cache Redis
 * Permet l'éviction manuelle et le monitoring du cache
 */
@Service
public class CacheManagementService {

    private static final Logger logger = LoggerFactory.getLogger(CacheManagementService.class);

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    public CacheManagementService(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Vide complètement le cache des prévisions météo
     */
    @CacheEvict(value = "forecasts", allEntries = true)
    public void evictAllForecasts() {
        logger.info("Evicting all forecasts from cache");
    }

    /**
     * Vide le cache pour une station spécifique
     */
    @CacheEvict(value = "forecasts", key = "'station:' + #stationId")
    public void evictForecastsForStation(Long stationId) {
        logger.info("Evicting forecasts for station {} from cache", stationId);
    }

    /**
     * Vide le cache pour une station et une période spécifique
     */
    @CacheEvict(value = "forecasts", key = "'station:' + #stationId + ':' + #startDate + ':' + #endDate")
    public void evictForecastsForPeriod(Long stationId, LocalDate startDate, LocalDate endDate) {
        logger.info("Evicting forecasts for station {} and period {}-{} from cache", 
            stationId, startDate, endDate);
    }

    /**
     * Vide tous les caches de l'application
     */
    @CacheEvict(value = {"forecasts", "stations", "weather-data"}, allEntries = true)
    public void evictAllCaches() {
        logger.info("Evicting all caches");
    }

    /**
     * Récupère les statistiques du cache Redis
     */
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();
        
        try {
            // Compte le nombre de clés pour chaque cache
            stats.setForecastsCacheSize(countKeys("arrosage:forecasts*"));
            stats.setStationsCacheSize(countKeys("arrosage:stations*"));
            stats.setWeatherDataCacheSize(countKeys("arrosage:weather-data*"));
            
            // Informations Redis
            stats.setRedisAvailable(isRedisAvailable());
            
            logger.debug("Cache stats: forecasts={}, stations={}, weather-data={}", 
                stats.getForecastsCacheSize(), 
                stats.getStationsCacheSize(), 
                stats.getWeatherDataCacheSize());
                
        } catch (Exception e) {
            logger.warn("Error getting cache stats: {}", e.getMessage());
            stats.setRedisAvailable(false);
        }
        
        return stats;
    }

    /**
     * Vérifie si Redis est disponible
     */
    public boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            logger.warn("Redis is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Compte le nombre de clés correspondant à un pattern
     */
    private long countKeys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            logger.warn("Error counting keys for pattern {}: {}", pattern, e.getMessage());
            return 0;
        }
    }

    /**
     * Classe pour les statistiques du cache
     */
    public static class CacheStats {
        private long forecastsCacheSize;
        private long stationsCacheSize;
        private long weatherDataCacheSize;
        private boolean redisAvailable;

        public long getForecastsCacheSize() {
            return forecastsCacheSize;
        }

        public void setForecastsCacheSize(long forecastsCacheSize) {
            this.forecastsCacheSize = forecastsCacheSize;
        }

        public long getStationsCacheSize() {
            return stationsCacheSize;
        }

        public void setStationsCacheSize(long stationsCacheSize) {
            this.stationsCacheSize = stationsCacheSize;
        }

        public long getWeatherDataCacheSize() {
            return weatherDataCacheSize;
        }

        public void setWeatherDataCacheSize(long weatherDataCacheSize) {
            this.weatherDataCacheSize = weatherDataCacheSize;
        }

        public boolean isRedisAvailable() {
            return redisAvailable;
        }

        public void setRedisAvailable(boolean redisAvailable) {
            this.redisAvailable = redisAvailable;
        }
    }
}
