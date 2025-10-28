package com.carpooling.backend.repository;

import com.carpooling.backend.model.Ride;
import com.carpooling.backend.model.RideStatus; // Import this
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime; // Import this
import java.util.List; // Import this

public interface RideRepository extends MongoRepository<Ride, String> {

    // --- NEW METHOD ---
    // This tells Spring Data to find all rides where:
    // 1. source matches (case-insensitive)
    // 2. destination matches (case-insensitive)
    // 3. status is UPCOMING
    // 4. rideDateTime is between the start of the day and the end of the day
    List<Ride> findBySourceIgnoreCaseAndDestinationIgnoreCaseAndStatusAndRideDateTimeBetween(
        String source, 
        String destination,
        RideStatus status,
        LocalDateTime startOfDay, 
        LocalDateTime endOfDay
    );
    // ... (inside RideRepository interface)
    
    // --- NEW METHODS FOR RIDE HISTORY ---
    
    // Find all rides where the driverId matches
    List<Ride> findByDriverId(String driverId);
    
    // Find all rides where the passengerIds list "contains" the given passengerId
    List<Ride> findByPassengerIdsContaining(String passengerId);
    
}