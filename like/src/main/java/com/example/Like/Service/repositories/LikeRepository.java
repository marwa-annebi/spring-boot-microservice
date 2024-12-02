package com.example.Like.Service.repositories;

import com.example.Like.Service.models.Like;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@EnableMongoRepositories

public interface LikeRepository extends MongoRepository<Like, String> {
}