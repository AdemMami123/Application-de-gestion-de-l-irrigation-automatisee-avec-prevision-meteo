package com.irrigation.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Configuration des routes du Gateway
 * Définit les règles de routage vers les microservices
 */
@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route vers Meteo Service
                .route("meteo-service", r -> r
                        .path("/api/meteo/**")
                        .filters(f -> f
                                .stripPrefix(1)  // Enlève /api du path
                                .circuitBreaker(config -> config
                                        .setName("meteoServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/meteo"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET))
                        )
                        .uri("lb://meteo-service"))  // Load balanced via Eureka
                
                // Route vers Arrosage Service
                .route("arrosage-service", r -> r
                        .path("/api/arrosage/**")
                        .filters(f -> f
                                .rewritePath("/api/arrosage/(?<segment>.*)", "/api/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("arrosageServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/arrosage"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET))
                        )
                        .uri("lb://arrosage-service"))
                
                // Route directe vers Eureka Dashboard (pour administration)
                .route("eureka-server", r -> r
                        .path("/eureka/**")
                        .uri("lb://eureka-server"))
                
                // Route pour actuator du gateway lui-même
                .route("gateway-actuator", r -> r
                        .path("/actuator/**")
                        .uri("forward:/actuator"))
                
                .build();
    }
}
