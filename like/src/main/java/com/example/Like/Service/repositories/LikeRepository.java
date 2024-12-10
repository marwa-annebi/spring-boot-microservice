package com.example.Like.Service.repositories;

import com.example.Like.Service.models.Like;
import com.example.Like.Service.models.Post;
import com.example.Like.Service.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@EnableMongoRepositories
@Repository

public interface LikeRepository extends MongoRepository<Like, String> {
    Optional<Like> findByLikedByAndPost(User likedBy, Post post);
    List<Like> findByPostId(String postId);

long countByPostAndTop(Post post, boolean isLike);
    boolean existsByLikedByAndPostAndTop(User likedBy, String postId, boolean isLike);
}

//    long countByPostAndTop(String postId, boolean isLike);
