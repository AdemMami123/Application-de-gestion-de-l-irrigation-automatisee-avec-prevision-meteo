package com.irrigation.arrosage.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Indicateur de santé personnalisé pour Redis
 * Intégré avec Spring Boot Actuator
 */
@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        try {
            String pong = redisConnectionFactory.getConnection().ping();
            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("status", "Redis is available")
                        .withDetail("response", pong)
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Redis ping returned unexpected response")
                        .withDetail("response", pong)
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Redis is not available")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
