package com.carpooling.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;

    private String rideId;        // Which ride this review is for
    private String reviewerId;    // User ID of the passenger writing the review
    private String reviewedDriverId; // User ID of the driver being reviewed
    private int rating;         // e.g., 1 to 5 stars
    private String comment;       // Optional text comment
    private LocalDateTime createdAt; // Timestamp

    public Review() {
        this.createdAt = LocalDateTime.now();
    }
}