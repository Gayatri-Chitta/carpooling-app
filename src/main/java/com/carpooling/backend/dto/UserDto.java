package com.carpooling.backend.dto;

import com.carpooling.backend.model.UserRole;

// This record will automatically have a constructor, getters, etc.
public record UserDto(
    String id,
    String name,
    String email,
    UserRole role,
    String phone,
    String vehicleModel,
    String vehicleNumber,
    boolean active
) {}