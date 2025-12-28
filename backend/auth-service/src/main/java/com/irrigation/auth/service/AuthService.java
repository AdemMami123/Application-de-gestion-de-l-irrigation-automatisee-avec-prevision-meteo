package com.irrigation.auth.service;

import com.irrigation.auth.dto.*;
import com.irrigation.auth.exception.TokenRefreshException;
import com.irrigation.auth.exception.UserAlreadyExistsException;
import com.irrigation.auth.exception.UserNotFoundException;
import com.irrigation.auth.model.RefreshToken;
import com.irrigation.auth.model.Role;
import com.irrigation.auth.model.User;
import com.irrigation.auth.repository.UserRepository;
import com.irrigation.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    
    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        
        log.info("Login successful for user: {}", user.getUsername());
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .user(mapToUserDTO(user))
            .build();
    }
    
    /**
     * Register new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Attempting registration for user: {}", registerRequest.getUsername());
        
        // Check if username exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("username", registerRequest.getUsername());
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("email", registerRequest.getEmail());
        }
        
        // Determine roles
        Set<Role> roles;
        if (registerRequest.getRoles() != null && !registerRequest.getRoles().isEmpty()) {
            roles = registerRequest.getRoles().stream()
                .map(this::mapToRole)
                .collect(Collectors.toSet());
        } else {
            // Default role
            roles = Set.of(Role.ROLE_OPERATOR);
        }
        
        // Create user
        User user = User.builder()
            .username(registerRequest.getUsername())
            .email(registerRequest.getEmail())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .firstName(registerRequest.getFirstName())
            .lastName(registerRequest.getLastName())
            .farmId(registerRequest.getFarmId())
            .roles(roles)
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();
        
        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());
        
        // Auto-login after registration
        String accessToken = jwtTokenProvider.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .user(mapToUserDTO(user))
            .build();
    }
    
    /**
     * Refresh access token
     */
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        
        return refreshTokenService.findByToken(requestRefreshToken)
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshToken::getUser)
            .map(user -> {
                String accessToken = jwtTokenProvider.generateToken(user);
                log.info("Token refreshed for user: {}", user.getUsername());
                return new TokenRefreshResponse(accessToken, requestRefreshToken, "Bearer");
            })
            .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, 
                "Refresh token is not in database!"));
    }
    
    /**
     * Logout user
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }
    
    /**
     * Validate access token
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
    
    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        
        User user = (User) authentication.getPrincipal();
        return mapToUserDTO(user);
    }
    
    /**
     * Get all users (Admin only)
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::mapToUserDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        return mapToUserDTO(user);
    }
    
    /**
     * Update user
     */
    @Transactional
    public UserDTO updateUser(Long id, RegisterRequest updateRequest) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        
        // Update fields
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new UserAlreadyExistsException("email", updateRequest.getEmail());
            }
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        if (updateRequest.getFarmId() != null) {
            user.setFarmId(updateRequest.getFarmId());
        }
        if (updateRequest.getRoles() != null && !updateRequest.getRoles().isEmpty()) {
            Set<Role> roles = updateRequest.getRoles().stream()
                .map(this::mapToRole)
                .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        user = userRepository.save(user);
        log.info("User updated: {}", user.getUsername());
        return mapToUserDTO(user);
    }
    
    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        refreshTokenService.deleteByUserId(id);
        userRepository.delete(user);
        log.info("User deleted: {}", user.getUsername());
    }
    
    /**
     * Toggle user enabled status
     */
    @Transactional
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(!user.isEnabled());
        user = userRepository.save(user);
        log.info("User status toggled for: {} - now {}", user.getUsername(), 
            user.isEnabled() ? "enabled" : "disabled");
        return mapToUserDTO(user);
    }
    
    /**
     * Map User to UserDTO
     */
    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .farmId(user.getFarmId())
            .roles(user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet()))
            .enabled(user.isEnabled())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
    
    /**
     * Map string to Role enum
     */
    private Role mapToRole(String roleName) {
        String roleStr = roleName.toUpperCase();
        if (!roleStr.startsWith("ROLE_")) {
            roleStr = "ROLE_" + roleStr;
        }
        try {
            return Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role: {}, defaulting to ROLE_OPERATOR", roleName);
            return Role.ROLE_OPERATOR;
        }
    }
}
