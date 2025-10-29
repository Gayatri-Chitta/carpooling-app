package com.carpooling.backend.config;

import com.carpooling.backend.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Keep this import
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // Keep this import
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Keep this import
import org.springframework.web.filter.CorsFilter; // Keep this import

import java.util.Arrays; // Keep this import

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Inject your JwtAuthFilter and CustomAuthEntryPoint
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthEntryPoint unauthorizedHandler;

    // Your constructor - make sure it includes these two
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CustomAuthEntryPoint unauthorizedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // You might want to allow your deployed frontend URL here too in the future
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173")); 
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        source.registerCorsConfiguration("/**", config); // Apply to all routes
        return new CorsFilter(source);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider,
                                                   CorsFilter corsFilter) throws Exception { 
        http
            // Add CORS filter first
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class) 
            .csrf(csrf -> csrf.disable()) // Disable CSRF
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(unauthorizedHandler) // Custom 401 handler
            )
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // --- Public Endpoints ---
                // Use requestMatchers for specific paths and permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll() 
                .requestMatchers("/api/v1/rides/search").permitAll()
                // Allow CORS preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                
                // --- All Other Endpoints must be authenticated ---
                .anyRequest().authenticated()
            )
            // Use stateless session management (essential for JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            )
            // Set the custom authentication provider
            .authenticationProvider(authenticationProvider)
            // Add our JWT filter *before* the standard authentication filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder); 
        authProvider.setUserDetailsService(userDetailsService); 
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}