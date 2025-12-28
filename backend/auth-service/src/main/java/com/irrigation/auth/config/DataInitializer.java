package com.irrigation.auth.config;

import com.irrigation.auth.model.Role;
import com.irrigation.auth.model.User;
import com.irrigation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Data initializer to create default admin user on startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.username:admin}")
    private String adminUsername;
    
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    @Value("${app.admin.email:admin@irrigation.local}")
    private String adminEmail;
    
    @Override
    public void run(String... args) throws Exception {
        createAdminUserIfNotExists();
    }
    
    private void createAdminUserIfNotExists() {
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .firstName("System")
                .lastName("Administrator")
                .roles(Set.of(Role.ROLE_ADMIN))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
            
            userRepository.save(admin);
            log.info("Default admin user created: {}", adminUsername);
        } else {
            log.info("Admin user already exists: {}", adminUsername);
        }
    }
}
