package com.carpooling.backend.service;

import com.carpooling.backend.dto.LoginRequest;
import com.carpooling.backend.dto.LoginResponse;
import com.carpooling.backend.dto.RegisterRequest;
import com.carpooling.backend.model.User;
import com.carpooling.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    // --- Dependencies ---
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- NEW Dependencies for Login ---
    @Autowired
    private AuthenticationManager authenticationManager; // From SecurityConfig

    @Autowired
    private JwtService jwtService; // Our new service

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Our new service

    // --- Registration Logic (from Step 3) ---
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    // --- NEW Login Logic ---
    public LoginResponse login(LoginRequest request) {
        // 1. Authenticate user
        // This will check if the email and password are correct
        // If not, it throws an exception
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );

        // 2. If authentication is successful, get user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        // 3. Generate JWT token
        String token = jwtService.generateToken(userDetails);

        // 4. Return the token in a response object
        return new LoginResponse(token);
    }
}