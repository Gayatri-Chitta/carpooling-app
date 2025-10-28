package com.carpooling.backend.controller;

import com.carpooling.backend.dto.ReviewRequest;
import com.carpooling.backend.model.Review;
import com.carpooling.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Import RequestMapping etc.

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Endpoint: POST /api/reviews/ride/{rideId}
    @PostMapping("/ride/{rideId}")
    public ResponseEntity<?> submitReview(
            @PathVariable String rideId,
            @Valid @RequestBody ReviewRequest request) {
        try {
            Review review = reviewService.submitReview(rideId, request);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Maybe add endpoints later to GET reviews
}