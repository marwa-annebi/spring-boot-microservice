package com.example.auth.repositories;

import com.example.auth.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    List<User> findByIdIn(java.util.List<java.lang.String> userIds);
}
