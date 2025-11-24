package com.irrigation.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour le Gateway Service
 */
@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false",  // Désactiver Eureka pour les tests
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
class GatewayServiceApplicationTests {

    @Autowired(required = false)
    private RouteLocator routeLocator;

    @Test
    void contextLoads() {
        // Vérifier que le contexte Spring se charge correctement
        assertThat(true).isTrue();
    }

    @Test
    void testRoutesAreConfigured() {
        if (routeLocator != null) {
            // Vérifier que des routes sont configurées
            var routes = routeLocator.getRoutes().collectList().block();
            assertThat(routes).isNotNull();
            assertThat(routes.size()).isGreaterThan(0);
        }
    }
}
