package com.irrigation.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Configuration du Rate Limiting basé sur Redis
 * Limite le nombre de requêtes par IP/utilisateur
 */
@Configuration
public class RateLimitConfig {

    /**
     * Résolveur de clé basé sur l'adresse IP du client
     * Utilisé pour limiter les requêtes par IP
     */
    @Primary
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = getClientIp(exchange);
            return Mono.just(clientIp);
        };
    }

    /**
     * Résolveur de clé basé sur le path de la requête
     * Permet de limiter les requêtes par endpoint
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

    /**
     * Résolveur de clé basé sur un header d'authentification
     * Utilisé pour limiter les requêtes par utilisateur
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) {
                return Mono.just(apiKey);
            }
            
            // Fallback sur l'IP si pas d'API key
            return Mono.just(getClientIp(exchange));
        };
    }

    /**
     * Extrait l'adresse IP réelle du client
     * Prend en compte les proxies (X-Forwarded-For)
     */
    private String getClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Prendre la première IP de la liste
            return xForwardedFor.split(",")[0].trim();
        }
        
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }
}
