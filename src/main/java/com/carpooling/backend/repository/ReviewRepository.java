package com.carpooling.backend.repository;

import com.carpooling.backend.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional; // Import Optional

public interface ReviewRepository extends MongoRepository<Review, String> {

    // Find reviews given to a specific driver
    List<Review> findByReviewedDriverId(String reviewedDriverId);

    // Find if a passenger has already reviewed a specific ride
    Optional<Review> findByRideIdAndReviewerId(String rideId, String reviewerId);
}