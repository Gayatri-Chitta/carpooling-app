package com.carpooling.backend.dto;

import com.carpooling.backend.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 'record' auto-creates constructor, getters, etc.
// We add validation rules from the 'Validation' dependency
public record RegisterRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @NotNull(message = "Role must be provided")
    UserRole role, // Will be "DRIVER" or "PASSENGER"

    String phone
) {}