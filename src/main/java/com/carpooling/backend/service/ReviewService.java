package com.carpooling.backend.service;

import com.carpooling.backend.dto.ReviewRequest;
import com.carpooling.backend.model.Review;
import com.carpooling.backend.model.Ride;
import com.carpooling.backend.model.User;
import com.carpooling.backend.repository.ReviewRepository;
import com.carpooling.backend.repository.RideRepository;
import com.carpooling.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private UserRepository userRepository;

    public Review submitReview(String rideId, ReviewRequest request) {
        // 1. Get passenger
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User passenger = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Get ride
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new RuntimeException("Ride not found"));

        // 3. Validation
        if (passenger.getRole() != com.carpooling.backend.model.UserRole.PASSENGER) {
            throw new IllegalStateException("Only passengers can submit reviews.");
        }
        if (ride.getStatus() != com.carpooling.backend.model.RideStatus.COMPLETED) {
            throw new RuntimeException("You can only review completed rides.");
        }
        if (!ride.getPassengerIds().contains(passenger.getId())) {
            throw new RuntimeException("You were not a passenger on this ride.");
        }
        // Check if already reviewed
        if (reviewRepository.findByRideIdAndReviewerId(rideId, passenger.getId()).isPresent()) {
             throw new RuntimeException("You have already reviewed this ride.");
        }

        // 4. Create and save review
        Review review = new Review();
        review.setRideId(rideId);
        review.setReviewerId(passenger.getId());
        review.setReviewedDriverId(ride.getDriverId()); // Get driver ID from the ride
        review.setRating(request.rating());
        review.setComment(request.comment());

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForDriver(String driverId) {
        return reviewRepository.findByReviewedDriverId(driverId);
    }

    // Calculate average rating
    public double getAverageRatingForDriver(String driverId) {
        List<Review> reviews = reviewRepository.findByReviewedDriverId(driverId);
        if (reviews.isEmpty()) {
            return 0.0; // Or maybe -1 or null to indicate no ratings yet
        }
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }
}