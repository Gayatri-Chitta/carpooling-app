package com.carpooling.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data; // From Lombok dependency

@Data // Lombok: auto-creates getters, setters, toString, etc.
@Document(collection = "users") // Tells Spring this is a MongoDB collection
public class User {

    @Id
    private String id; // MongoDB will auto-generate this

    private String name;

    @Indexed(unique = true) // Ensures no duplicate emails
    private String email;

    private String password; // This will store the HASHED password

    private UserRole role;

    private String phone;

    private boolean active = true; // Default to true when a user registers
    
    // ... (inside User class, after 'private String phone;')

    // Vehicle details (for DRIVER role only)
    private String vehicleModel;
    private String vehicleNumber;
}