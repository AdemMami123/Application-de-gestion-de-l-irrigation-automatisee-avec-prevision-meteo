package com.irrigation.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    @Value("${jwt.secret:IrrigationSecretKey2024!@#$%^&*()VeryLongSecretKeyForJWT}")
    private String jwtSecret;
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // Paths that don't require authentication
    private final List<String> publicPaths = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh-token",
        "/api/auth/validate",
        "/actuator/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/eureka/**"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        // Get authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No valid authorization header found for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }
        
        String token = authHeader.substring(7);
        
        try {
            // Validate token
            Claims claims = validateToken(token);
            
            // Add user info to request headers for downstream services
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Name", claims.getSubject())
                .header("X-User-Roles", extractRoles(claims))
                .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            return onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return onError(exchange, "Invalid token signature", HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return onError(exchange, "Malformed token", HttpStatus.UNAUTHORIZED);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return onError(exchange, "Unsupported token", HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
        }
    }
    
    private boolean isPublicPath(String path) {
        return publicPaths.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    private Claims validateToken(String token) {
        SecretKey key = getSigningKey();
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    private String extractRoles(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (rolesObj != null) {
            return rolesObj.toString();
        }
        return "";
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\":\"%s\",\"status\":%d}", message, status.value());
        
        return response.writeWith(Mono.just(
            response.bufferFactory().wrap(body.getBytes())
        ));
    }
    
    @Override
    public int getOrder() {
        // Run before other filters
        return -100;
    }
}
