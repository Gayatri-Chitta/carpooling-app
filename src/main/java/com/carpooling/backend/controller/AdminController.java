package com.carpooling.backend.controller;

import com.carpooling.backend.dto.UserDto;
import com.carpooling.backend.model.User;
import com.carpooling.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/admin") // Base URL for all admin endpoints
public class AdminController {

    @Autowired
    private UserService userService;

    // This endpoint is for: GET http://localhost:8081/api/admin/users
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // <-- This is the magic!
    public ResponseEntity<List<User>> getAllUsers() {
        // This line will only run if the user has the 'ROLE_ADMIN'
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    // ... (inside AdminController class)

    // Endpoint will be: PUT http://localhost:8081/api/admin/users/{userId}/status?active=true
    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setUserStatus(
            @PathVariable String userId, 
            @RequestParam boolean active) {
        try {
            User updatedUser = userService.setUserStatus(userId, active);
            return ResponseEntity.ok(new UserDto( // Return the safe DTO
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                updatedUser.getPhone(),
                updatedUser.getVehicleModel(),
                updatedUser.getVehicleNumber(),
                updatedUser.isActive()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}