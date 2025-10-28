package com.carpooling.backend.controller;

import com.carpooling.backend.dto.UserDto;
import com.carpooling.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpooling.backend.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.carpooling.backend.service.ReviewService; // <-- Import
import java.util.Map; // <-- Import

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // This creates the endpoint: GET http://localhost:8081/api/users/me
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyUserProfile() {
        // This endpoint is automatically protected by our SecurityConfig
        // It will only work if a valid token is sent
        UserDto userDto = userService.getUserProfile();
        return ResponseEntity.ok(userDto);
    }

    // ... (inside UserController class)

    // This creates the endpoint: PUT http://localhost:8081/api/users/me
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMyUserProfile(@Valid @RequestBody UpdateProfileRequest request) {
        // This endpoint is also protected and uses the logged-in user's token
        UserDto updatedUser = userService.updateUserProfile(request);
        return ResponseEntity.ok(updatedUser);
    }

    
    
    @Autowired // <-- Inject ReviewService
    private ReviewService reviewService;

    // ... (get /me endpoint)
    // ... (put /me endpoint)

    // Endpoint: GET /api/users/{userId}/rating
    @GetMapping("/{userId}/rating")
    public ResponseEntity<?> getUserAverageRating(@PathVariable String userId) {
        // This could be public if you want anyone to see ratings
        try {
            double avgRating = reviewService.getAverageRatingForDriver(userId);
            // Return it in a simple JSON object
            return ResponseEntity.ok(Map.of("averageRating", avgRating));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}