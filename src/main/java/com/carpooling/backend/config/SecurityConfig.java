package com.carpooling.backend.config;

import com.carpooling.backend.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- IMPORT THIS
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
import org.springframework.web.cors.CorsConfiguration; // <-- IMPORT THIS
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // <-- IMPORT THIS
import org.springframework.web.filter.CorsFilter; // <-- IMPORT THIS

import java.util.Arrays; // <-- IMPORT THIS

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- THIS IS THE NEW CORS CONFIGURATION BEAN ---
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // Your frontend URL
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        source.registerCorsConfiguration("/**", config); // Apply to all routes
        return new CorsFilter(source);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 AuthenticationProvider authenticationProvider,
                                                 JwtAuthFilter jwtAuthFilter,
                                                 CustomAuthEntryPoint unauthorizedHandler,
                                                 CorsFilter corsFilter) throws Exception { // <-- Inject CorsFilter
        http
            // 1. Add our new corsFilter (this must come BEFORE Spring Security's filter)
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(unauthorizedHandler)
            )
            .authorizeHttpRequests(authz -> authz
                // 2. Explicitly allow preflight OPTIONS requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // --- Public Endpoints ---
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/rides/search").permitAll()
                
                // --- All Other Endpoints ---
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            // 3. We moved the corsFilter to run earlier
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