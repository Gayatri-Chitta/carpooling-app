package com.carpooling.backend.controller;

// Import these
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carpooling.backend.dto.LoginRequest;
import com.carpooling.backend.dto.LoginResponse;
import com.carpooling.backend.dto.RegisterRequest;
import com.carpooling.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Add this logger
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        
        // --- ADD THIS LINE ---
        logger.info("REGISTER ATTEMPT received for email: {}", registerRequest.email());
        // --- END OF ADD ---

        try {
            authService.register(registerRequest);
            logger.info("REGISTER SUCCESS for email: {}", registerRequest.email());
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) {
            // Add a logger for errors too
            logger.error("REGISTER FAILED for {}: {}", registerRequest.email(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        // --- ADD THIS LINE ---
        logger.info("LOGIN ATTEMPT received for email: {}", loginRequest.email());
        // --- END OF ADD ---
        
        try {
            LoginResponse response = authService.login(loginRequest);
            logger.info("LOGIN SUCCESS for email: {}", loginRequest.email());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            logger.warn("LOGIN FAILED for {}: {}", loginRequest.email(), e.getMessage());
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
}