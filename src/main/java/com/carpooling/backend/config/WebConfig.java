package com.carpooling.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Allow all paths under /api/
            .allowedOrigins("http://localhost:5173") // Your React app's URL
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed methods
            .allowedHeaders("*") // Allow all headers
            .allowCredentials(true);
    }
}