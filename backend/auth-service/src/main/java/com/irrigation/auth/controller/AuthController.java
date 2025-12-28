package com.irrigation.auth.controller;

import com.irrigation.auth.dto.*;
import com.irrigation.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUsername());
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * User registration
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration request received for user: {}", registerRequest.getUsername());
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Refresh access token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh request received");
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Logout user
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody TokenRefreshRequest request) {
        log.info("Logout request received");
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(new MessageResponse("User logged out successfully"));
    }
    
    /**
     * Validate token
     */
    @GetMapping("/validate")
    public ResponseEntity<MessageResponse> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("Token is valid"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new MessageResponse("Token is invalid or expired"));
    }
    
    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get all users (Admin/Manager only)
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Fetching all users");
        List<UserDTO> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID (Admin/Manager only)
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        UserDTO user = authService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Update user (Admin only)
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody RegisterRequest updateRequest) {
        log.info("Updating user with id: {}", id);
        UserDTO user = authService.updateUser(id, updateRequest);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Delete user (Admin only)
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        authService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }
    
    /**
     * Toggle user enabled status (Admin only)
     */
    @PatchMapping("/users/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> toggleUserStatus(@PathVariable Long id) {
        log.info("Toggling status for user with id: {}", id);
        UserDTO user = authService.toggleUserStatus(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Create admin user (Admin only, for bootstrap)
     */
    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> createUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Admin creating user: {}", registerRequest.getUsername());
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
