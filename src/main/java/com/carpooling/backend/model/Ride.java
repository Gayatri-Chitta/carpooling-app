package com.carpooling.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
@Document(collection = "rides") // This will be a new collection in MongoDB
public class Ride {

    @Id
    private String id;

    private String driverId; // The User ID of the driver
    private String driverName;

    private String source;
    private String destination;

    private LocalDateTime rideDateTime; // Date and Time of departure

    private int availableSeats;
    private double pricePerSeat;

    private RideStatus status;

    
    // ... (inside Ride class, after 'private RideStatus status;')

    private List<String> passengerIds = new ArrayList<>();
}