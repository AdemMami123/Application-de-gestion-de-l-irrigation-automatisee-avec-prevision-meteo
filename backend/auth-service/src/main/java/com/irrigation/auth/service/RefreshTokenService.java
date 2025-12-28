package com.irrigation.auth.service;

import com.irrigation.auth.exception.TokenRefreshException;
import com.irrigation.auth.model.RefreshToken;
import com.irrigation.auth.model.User;
import com.irrigation.auth.repository.RefreshTokenRepository;
import com.irrigation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    
    /**
     * Find refresh token by token string
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    /**
     * Create new refresh token for user
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Delete existing refresh token for user
        refreshTokenRepository.deleteByUser(user);
        
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
            .build();
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * Verify if refresh token is expired
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), 
                "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
    
    /**
     * Delete refresh token by user
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }
    
    /**
     * Delete refresh token by token string
     */
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
            .ifPresent(refreshTokenRepository::delete);
    }
}
