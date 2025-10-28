package com.carpooling.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record OfferRideRequest(
    @NotBlank String source,
    @NotBlank String destination,

    @NotNull @Future(message = "Ride must be in the future")
    LocalDateTime rideDateTime,

    @NotNull @Min(1)
    Integer availableSeats,

    @NotNull @Min(0)
    Double pricePerSeat
) {}