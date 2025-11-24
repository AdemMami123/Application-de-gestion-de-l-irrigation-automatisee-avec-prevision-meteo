package com.irrigation.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Service - Point d'entrée unique pour tous les microservices
 * 
 * Fonctionnalités:
 * - Routage dynamique vers les microservices
 * - Rate limiting basé sur Redis
 * - Circuit breaker pour la résilience
 * - Logging des requêtes/réponses
 * - Gestion des CORS
 * - Corrélation des requêtes
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
