package com.irrigation.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CorrelationIdFilter
 */
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void testFilterAddsCorrelationIdWhenNotPresent() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // Act
        filter.filter(exchange, chain).block();

        // Assert
        String correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-Id");
        assertThat(correlationId).isNotNull();
        assertThat(correlationId).isNotEmpty();
    }

    @Test
    void testFilterPreservesExistingCorrelationId() {
        // Arrange
        String existingId = "test-correlation-id";
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost/test")
                .header("X-Correlation-Id", existingId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // Act
        filter.filter(exchange, chain).block();

        // Assert
        String correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-Id");
        assertThat(correlationId).isEqualTo(existingId);
    }

    @Test
    void testFilterOrder() {
        // Vérifier que le filtre a la plus haute priorité
        assertThat(filter.getOrder()).isEqualTo(Integer.MIN_VALUE);
    }
}
