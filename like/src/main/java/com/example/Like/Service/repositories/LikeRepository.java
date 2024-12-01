package com.example.Like.Service.repositories;

import com.example.Like.Service.models.Like;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface LikeRepository extends MongoRepository<Like, String> {
    Optional<Like> findByPostIdAndUserId(String postId, String userId);
    void deleteByPostIdAndUserId(String postId, String userId);
    long countByPostId(String postId);
}