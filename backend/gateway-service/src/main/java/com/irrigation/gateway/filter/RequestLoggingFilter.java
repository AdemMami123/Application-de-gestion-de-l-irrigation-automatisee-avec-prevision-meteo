package com.irrigation.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtre global pour logger toutes les requêtes et réponses
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getId();
        String method = exchange.getRequest().getMethod().toString();
        String path = exchange.getRequest().getURI().getPath();
        String clientIp = getClientIp(exchange);
        
        log.info("Incoming Request - ID: {}, Method: {}, Path: {}, Client IP: {}", 
                requestId, method, path, clientIp);
        
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    int statusCode = exchange.getResponse().getStatusCode() != null 
                            ? exchange.getResponse().getStatusCode().value() 
                            : 0;
                    
                    log.info("Outgoing Response - ID: {}, Status: {}, Path: {}", 
                            requestId, statusCode, path);
                }));
    }

    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "Unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
