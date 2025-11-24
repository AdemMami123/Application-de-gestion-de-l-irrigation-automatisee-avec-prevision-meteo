package com.irrigation.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Configuration CORS pour le Gateway
 * Permet aux applications frontend d'accéder aux APIs
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Origines autorisées (à adapter selon votre frontend)
        // Utilisation de setAllowedOriginPatterns au lieu de setAllowedOrigins
        // pour supporter allowCredentials=true
        corsConfig.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",     // All localhost ports
                "http://127.0.0.1:*"      // All 127.0.0.1 ports
        ));
        
        // Méthodes HTTP autorisées
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Headers autorisés
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Correlation-Id",
                "X-Request-Id",
                "Accept"
        ));
        
        // Headers exposés au client
        corsConfig.setExposedHeaders(Arrays.asList(
                "X-Correlation-Id",
                "X-Response-Time"
        ));
        
        // Autoriser les credentials (cookies, auth headers)
        corsConfig.setAllowCredentials(true);
        
        // Durée de cache de la config CORS (en secondes)
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
