package com.carpooling.backend.controller;

import com.carpooling.backend.dto.OfferRideRequest;
import com.carpooling.backend.model.Ride;
import com.carpooling.backend.service.RideService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus; // For returning 204 No Content

import com.carpooling.backend.dto.UpdateRideRequest;
import org.springframework.web.bind.annotation.PutMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/rides") // Base URL for all ride-related endpoints
public class RideController {

    @Autowired
    private RideService rideService;

    @PostMapping("/offer") // This makes the endpoint: POST http://localhost:8081/api/rides/offer
    public ResponseEntity<?> offerRide(@Valid @RequestBody OfferRideRequest offerRideRequest) {
        try {
            Ride newRide = rideService.offerRide(offerRideRequest);
            return ResponseEntity.ok(newRide);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ... (inside RideController class)

    // --- NEW SEARCH ENDPOINT ---
    @GetMapping("/search")
    public ResponseEntity<?> searchRides(
        @RequestParam String source,
        @RequestParam String destination,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // @RequestParam reads data from the URL query string (e.g., ?source=Downtown)
        // @DateTimeFormat tells Spring how to parse the date string (e.g., 2025-11-20)
        
        try {
            List<Ride> availableRides = rideService.searchRides(source, destination, date);
            
            if (availableRides.isEmpty()) {
                return ResponseEntity.ok("No available rides found for this route on the selected date.");
            }
            
            return ResponseEntity.ok(availableRides);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error searching for rides: " + e.getMessage());
        }
    }

    // ... (inside RideController class)

    // --- NEW BOOKING ENDPOINT ---
    @PostMapping("/{rideId}/book")
    public ResponseEntity<?> bookRide(@PathVariable String rideId) {
        // @PathVariable grabs the {rideId} from the URL
        
        try {
            Ride updatedRide = rideService.bookRide(rideId);
            return ResponseEntity.ok(updatedRide);
        } catch (Exception e) {
            // Return a 400 Bad Request with the specific error message
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ... (inside RideController class)

    // --- NEW ENDPOINT: Get rides as DRIVER ---
    @GetMapping("/my-rides/driver")
    public ResponseEntity<?> getMyRidesAsDriver() {
        try {
            List<Ride> rides = rideService.getRidesAsDriver();
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- NEW ENDPOINT: Get rides as PASSENGER ---
    @GetMapping("/my-rides/passenger")
    public ResponseEntity<?> getMyRidesAsPassenger() {
        try {
            List<Ride> rides = rideService.getRidesAsPassenger();
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ... (inside RideController class)

    // This creates the endpoint: POST http://localhost:8081/api/rides/{rideId}/cancel
    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable String rideId) {
        // This is a protected endpoint
        try {
            Ride updatedRide = rideService.cancelBooking(rideId);
            return ResponseEntity.ok(updatedRide);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ... (inside RideController class)

    // This creates the endpoint: DELETE http://localhost:8081/api/rides/{rideId}
    @DeleteMapping("/{rideId}")
    public ResponseEntity<?> cancelOfferedRide(@PathVariable String rideId) {
        // This is a protected endpoint
        try {
            rideService.cancelOfferedRide(rideId);
            // Return 204 No Content on successful deletion/cancellation
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); 
        } catch (SecurityException e) {
            // If user is not the driver
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ... (inside RideController class)

    // Endpoint: POST http://localhost:8081/api/rides/{rideId}/complete
    @PostMapping("/{rideId}/complete")
    public ResponseEntity<?> completeRide(@PathVariable String rideId) {
        try {
            Ride updatedRide = rideService.completeRide(rideId);
            return ResponseEntity.ok(updatedRide);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ... (inside RideController class)

    // Endpoint: PUT http://localhost:8081/api/rides/{rideId}
    @PutMapping("/{rideId}")
    public ResponseEntity<?> editRide(
            @PathVariable String rideId,
            @Valid @RequestBody UpdateRideRequest request) {
        try {
            Ride updatedRide = rideService.editRide(rideId, request);
            return ResponseEntity.ok(updatedRide);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint: GET http://localhost:8081/api/rides/{rideId}
    @GetMapping("/{rideId}")
    public ResponseEntity<?> getRideById(@PathVariable String rideId) {
        // This is protected, only logged-in users can see details?
        // Or make it public? Let's keep it protected for now.
        try {
            Ride ride = rideService.getRideDetails(rideId);
            return ResponseEntity.ok(ride);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}