package com.carpooling.backend.service;

import com.carpooling.backend.dto.OfferRideRequest;
import com.carpooling.backend.dto.UpdateRideRequest;
import com.carpooling.backend.model.Ride;
import com.carpooling.backend.model.RideStatus;
import com.carpooling.backend.model.User;
import com.carpooling.backend.model.UserRole;
import com.carpooling.backend.repository.RideRepository;
import com.carpooling.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Service
public class RideService {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository; // To get driver details

    public Ride offerRide(OfferRideRequest request) {
        // 1. Get the authenticated user's email from the security context
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the user in the database
        User driver = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. Check if the user is a DRIVER
        if (driver.getRole() != UserRole.DRIVER) {
            throw new IllegalStateException("Only users with DRIVER role can offer a ride");
        }

        // 4. Create a new Ride object
        Ride ride = new Ride();
        ride.setSource(request.source());
        ride.setDestination(request.destination());
        ride.setRideDateTime(request.rideDateTime());
        ride.setAvailableSeats(request.availableSeats());
        ride.setPricePerSeat(request.pricePerSeat());

        // 5. Link the ride to the driver
        ride.setDriverId(driver.getId());
        ride.setDriverName(driver.getName());

        // 6. Set the initial status
        ride.setStatus(RideStatus.UPCOMING);

        // 7. Save the ride to the database
        return rideRepository.save(ride);
    }

    // ... (inside RideService class)

    // --- NEW SEARCH METHOD ---
    public List<Ride> searchRides(String source, String destination, LocalDate date) {
        
        // 1. Calculate the start and end of the given date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 2. Call the new repository method
        List<Ride> rides = rideRepository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndStatusAndRideDateTimeBetween(
            source,
            destination,
            RideStatus.UPCOMING, // We only want to find upcoming rides
            startOfDay,
            endOfDay
        );

        // 3. Return the list of found rides
        return rides;
    }

    // ... (inside RideService class)

    // --- NEW BOOKING METHOD ---
    public Ride bookRide(String rideId) {
        // 1. Get the authenticated user's email
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Find the user (the passenger)
        User passenger = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. Find the ride they want to book
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new RuntimeException("Ride not found"));

        // 4. --- Validation Checks ---
        
        // Check if user is a PASSENGER
        if (passenger.getRole() != UserRole.PASSENGER) {
            throw new IllegalStateException("Only users with PASSENGER role can book a ride");
        }
        
        // Check if the ride is full
        if (ride.getAvailableSeats() <= 0) {
            throw new RuntimeException("Ride is already full");
        }

        // Check if the ride is still UPCOMING
        if (ride.getStatus() != RideStatus.UPCOMING) {
            throw new RuntimeException("This ride is not available for booking");
        }
        
        // Check if the passenger is the driver
        if (ride.getDriverId().equals(passenger.getId())) {
            throw new RuntimeException("You cannot book your own ride");
        }

        // Check if passenger has already booked this ride
        if (ride.getPassengerIds().contains(passenger.getId())) {
            throw new RuntimeException("You have already booked this ride");
        }

        // 5. --- All checks passed! Book the ride ---
        
        // Add passenger to the list
        ride.getPassengerIds().add(passenger.getId());
        
        // Decrement available seats
        ride.setAvailableSeats(ride.getAvailableSeats() - 1);

        // 6. Save the updated ride
        return rideRepository.save(ride);
    }

    // ... (inside RideService class)

    // --- NEW METHOD: Get rides as a DRIVER ---
    public List<Ride> getRidesAsDriver() {
        // 1. Get the authenticated user's email
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Find the user (the driver)
        User driver = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
        // 3. Call the repository method
        return rideRepository.findByDriverId(driver.getId());
    }

    // --- NEW METHOD: Get rides as a PASSENGER ---
    public List<Ride> getRidesAsPassenger() {
        // 1. Get the authenticated user's email
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Find the user (the passenger)
        User passenger = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
        // 3. Call the repository method
        return rideRepository.findByPassengerIdsContaining(passenger.getId());
    }

    // ... (inside RideService class)

    public Ride cancelBooking(String rideId) {
        // 1. Get the authenticated user's email
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the user (the passenger)
        User passenger = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. Find the ride they want to cancel
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new RuntimeException("Ride not found"));

        // 4. --- Validation Checks ---

        // Check if the user is a PASSENGER
        if (passenger.getRole() != com.carpooling.backend.model.UserRole.PASSENGER) {
            throw new IllegalStateException("Only passengers can cancel a booking.");
        }

        // Check if the ride is still UPCOMING (you can't cancel a completed ride)
        if (ride.getStatus() != RideStatus.UPCOMING) {
            throw new RuntimeException("This ride cannot be cancelled.");
        }

        // Check if the passenger has actually booked this ride
        if (!ride.getPassengerIds().contains(passenger.getId())) {
            throw new RuntimeException("You have not booked this ride.");
        }

        // 5. --- All checks passed! Cancel the booking ---

        // Remove passenger from the list
        ride.getPassengerIds().remove(passenger.getId());

        // Increment available seats
        ride.setAvailableSeats(ride.getAvailableSeats() + 1);

        // 6. Save the updated ride
        return rideRepository.save(ride);
    }

    // ... (inside RideService class)

    public void cancelOfferedRide(String rideId) {
        // 1. Get the authenticated user's email
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the user (the driver)
        User driver = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. Find the ride
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new RuntimeException("Ride not found"));

        // 4. --- Validation Checks ---

        // Check if the user is a DRIVER
        if (driver.getRole() != com.carpooling.backend.model.UserRole.DRIVER) {
            throw new IllegalStateException("Only the driver can cancel an offered ride.");
        }

        // Check if this driver actually owns this ride
        if (!ride.getDriverId().equals(driver.getId())) {
            throw new SecurityException("You are not authorized to cancel this ride.");
        }

        // Check if the ride is still UPCOMING 
        // (Maybe allow cancelling COMPLETED rides if needed for cleanup?)
        if (ride.getStatus() != RideStatus.UPCOMING) {
            throw new RuntimeException("Only upcoming rides can be cancelled.");
        }

        // Optional: Check if anyone has booked it?
        // You might decide a driver can't cancel if passengers are booked,
        // or you might just notify them. For simplicity now, we allow it.

        // 5. --- All checks passed! Cancel the ride ---

        // Option 1: Delete the ride completely
        // rideRepository.delete(ride); 

        // Option 2: Mark the ride as CANCELLED (Better for history)
        ride.setStatus(RideStatus.CANCELLED);
        ride.setAvailableSeats(0); // No more bookings
        rideRepository.save(ride);

        // TODO: In a real app, you would also notify booked passengers here.
    }

    // ... (inside RideService class)

    public Ride completeRide(String rideId) {
        // 1. Get authenticated user (should be the driver)
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Find the ride
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new RuntimeException("Ride not found"));

        // 3. Validation
        if (driver.getRole() != com.carpooling.backend.model.UserRole.DRIVER) {
            throw new IllegalStateException("Only the driver can mark the ride as completed.");
        }
        if (!ride.getDriverId().equals(driver.getId())) {
            throw new SecurityException("You are not the driver of this ride.");
        }
        if (ride.getStatus() != RideStatus.UPCOMING) {
            throw new RuntimeException("Ride must be UPCOMING to be marked as completed.");
        }
        // Optional: Check if rideDateTime is in the past

        // 4. Update status
        ride.setStatus(RideStatus.COMPLETED);
        return rideRepository.save(ride);
    }

    // ... (inside RideService class)

    public Ride editRide(String rideId, UpdateRideRequest request) {
        // 1. Get driver
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Find ride
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new RuntimeException("Ride not found"));

        // 3. Validation
        if (driver.getRole() != com.carpooling.backend.model.UserRole.DRIVER) {
            throw new IllegalStateException("Only drivers can edit rides.");
        }
        if (!ride.getDriverId().equals(driver.getId())) {
            throw new SecurityException("You are not the driver of this ride.");
        }
        if (ride.getStatus() != RideStatus.UPCOMING) {
            throw new RuntimeException("Only upcoming rides can be edited.");
        }
        // Validate available seats vs booked seats
        int bookedSeats = ride.getPassengerIds().size();
        if (request.availableSeats() < bookedSeats) {
            throw new RuntimeException("Available seats cannot be less than the number of passengers already booked (" + bookedSeats + ").");
        }

        // 4. Update fields
        ride.setSource(request.source());
        ride.setDestination(request.destination());
        ride.setRideDateTime(request.rideDateTime());
        ride.setAvailableSeats(request.availableSeats());
        ride.setPricePerSeat(request.pricePerSeat());

        // 5. Save and return
        return rideRepository.save(ride);
        // TODO: Notify booked passengers about the changes
    }

    public Ride getRideDetails(String rideId) {
        return rideRepository.findById(rideId)
            .orElseThrow(() -> new RuntimeException("Ride not found"));
    }
}