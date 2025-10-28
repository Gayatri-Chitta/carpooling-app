package com.carpooling.backend.dto;

import jakarta.validation.constraints.NotBlank;

// Defines the fields a user can update
public record UpdateProfileRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,

    String phone,
    String vehicleModel,
    String vehicleNumber
) {}