package com.irrigation.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtre global pour mesurer le temps de réponse des requêtes
 */
@Slf4j
@Component
public class ResponseTimeFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_START_TIME_ATTR = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Enregistrer le temps de début
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    String path = exchange.getRequest().getURI().getPath();
                    String method = exchange.getRequest().getMethod().toString();
                    
                    // Log seulement (ne pas modifier les headers après commit)
                    log.info("Request completed - Method: {}, Path: {}, Duration: {}ms", 
                            method, path, duration);
                    
                    // Alerter si le temps de réponse est élevé
                    if (duration > 5000) {
                        log.warn("Slow request detected - Path: {}, Duration: {}ms", path, duration);
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
