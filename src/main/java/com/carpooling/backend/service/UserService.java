package com.carpooling.backend.service;

import com.carpooling.backend.model.User;
import com.carpooling.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import com.carpooling.backend.dto.UserDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.carpooling.backend.dto.UpdateProfileRequest;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        // Simply return all users from the repository
        return userRepository.findAll();
    }
    // ... (inside UserService class, below getAllUsers method)

    public UserDto getUserProfile() {
        // 1. Get the email of the currently logged-in user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the user in the database
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 3. Map the User object to our safe UserDto
        return new UserDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getPhone(),
            user.getVehicleModel(),
            user.getVehicleNumber(),
            user.isActive()
        );
    }
    // ... (inside UserService class)

    public User setUserStatus(String userId, boolean active) {
        // Find the user by their ID
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Set their new status
        user.setActive(active);

        // Save and return the updated user
        return userRepository.save(user);
    }

    // ... (inside UserService class)

    public UserDto updateUserProfile(UpdateProfileRequest request) {
        // 1. Get the email of the currently logged-in user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the user
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. Update the allowed fields
        user.setName(request.name());
        user.setPhone(request.phone());

        // Only update vehicle details if the user is a DRIVER
        if (user.getRole() == com.carpooling.backend.model.UserRole.DRIVER) {
            user.setVehicleModel(request.vehicleModel());
            user.setVehicleNumber(request.vehicleNumber());
        }

        // 4. Save the updated user
        User updatedUser = userRepository.save(user);

        // 5. Return the safe DTO
        return new UserDto(
            updatedUser.getId(),
            updatedUser.getName(),
            updatedUser.getEmail(),
            updatedUser.getRole(),
            updatedUser.getPhone(),
            updatedUser.getVehicleModel(),
            updatedUser.getVehicleNumber(),
            updatedUser.isActive()
        );
    }
}