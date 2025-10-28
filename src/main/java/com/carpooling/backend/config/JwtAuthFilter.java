package com.carpooling.backend.config;

import com.carpooling.backend.service.JwtService;
import com.carpooling.backend.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

// --- ADD THESE IMPORTS ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// --- END IMPORTS ---

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    // --- ADD THIS LOGGER ---
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // --- THIS IS THE UPDATED METHOD WITH LOGGING ---
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Log for public paths (like /login or /search)
            if (!request.getServletPath().startsWith("/api/auth/")) {
                 logger.warn("JWT Filter: No Bearer token found in request for protected path: {}", request.getServletPath());
            }
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); 
        logger.info("JWT Filter: Token received: {}", jwt);

        try {
            userEmail = jwtService.extractUsername(jwt);
            logger.info("JWT Filter: Email extracted from token: {}", userEmail);
        } catch (Exception e) {
            logger.error("JWT Filter: FAILED to extract username from token. Error: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("JWT Filter: Loading UserDetails for {}", userEmail);
            UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            } catch (Exception e) {
                logger.error("JWT Filter: FAILED to load user details for {}", userEmail, e);
                filterChain.doFilter(request, response);
                return;
            }
            
            if (jwtService.isTokenValid(jwt, userDetails)) {
                logger.info("JWT Filter: Token is VALID. Authorities: {}. Setting authentication context.", userDetails.getAuthorities());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                 logger.warn("JWT Filter: Token is INVALID for user {}", userEmail);
            }
        } else {
             logger.warn("JWT Filter: userEmail is null or Authentication is already set.");
        }
        
        filterChain.doFilter(request, response);
    }
}