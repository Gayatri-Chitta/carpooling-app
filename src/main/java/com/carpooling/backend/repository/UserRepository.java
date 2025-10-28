package com.carpooling.backend.repository;

import com.carpooling.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

// We extend MongoRepository, giving it our User model and the type of its ID (String)
public interface UserRepository extends MongoRepository<User, String> {

    // Spring Data automatically creates this query for us
    // It's used to check if an email already exists
    Optional<User> findByEmail(String email);
}