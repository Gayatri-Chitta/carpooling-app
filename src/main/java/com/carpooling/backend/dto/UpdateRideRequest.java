package com.carpooling.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

// Fields a driver can modify for an UPCOMING ride
public record UpdateRideRequest(
    @NotBlank String source,
    @NotBlank String destination,
    @NotNull @Future LocalDateTime rideDateTime,
    @NotNull @Min(0) Integer availableSeats, // Min 0 allows setting seats based on current bookings
    @NotNull @Min(0) Double pricePerSeat
) {}