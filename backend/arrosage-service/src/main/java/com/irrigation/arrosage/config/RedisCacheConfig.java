package com.irrigation.arrosage.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Redis pour le cache des prévisions météo
 * 
 * Features:
 * - Cache avec TTL de 2 heures pour les prévisions
 * - Serialization JSON avec Jackson
 * - Gestion des erreurs Redis (fallback)
 * - Support des types Java 8 (LocalDate, LocalDateTime)
 */
@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);

    /**
     * Configuration du CacheManager Redis avec différents TTL par cache
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configuration par défaut: 2 heures de TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                createJsonSerializer()
                        )
                );

        // Configurations spécifiques par cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Cache des prévisions météo: 2 heures
        cacheConfigurations.put("forecasts", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Cache des stations météo: 24 heures (données plus stables)
        cacheConfigurations.put("stations", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Cache des données météo courantes: 30 minutes
        cacheConfigurations.put("weather-data", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * RedisTemplate pour les opérations manuelles sur Redis
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(createJsonSerializer());
        template.setHashValueSerializer(createJsonSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Gestionnaire d'erreurs pour le cache Redis
     * En cas d'erreur, l'application continue de fonctionner sans cache
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                logger.warn("Cache GET error for cache '{}' and key '{}': {}", 
                    cache.getName(), key, exception.getMessage());
                // Continue sans cache en cas d'erreur
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                logger.warn("Cache PUT error for cache '{}' and key '{}': {}", 
                    cache.getName(), key, exception.getMessage());
                // Continue sans cache en cas d'erreur
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                logger.warn("Cache EVICT error for cache '{}' and key '{}': {}", 
                    cache.getName(), key, exception.getMessage());
                // Continue sans cache en cas d'erreur
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                logger.warn("Cache CLEAR error for cache '{}': {}", 
                    cache.getName(), exception.getMessage());
                // Continue sans cache en cas d'erreur
            }
        };
    }

    /**
     * Crée un serializer JSON avec support des types Java 8 Date/Time
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Support des types Java 8 Date/Time (LocalDate, LocalDateTime, etc.)
        objectMapper.registerModule(new JavaTimeModule());
        
        // Active le type information pour la désérialisation polymorphique
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
