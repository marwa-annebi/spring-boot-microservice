package com.example.Comment.Service.repositories;

import com.example.Comment.Service.models.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostId(String post);
    long countByPostId(String postId);

    List<Comment> findByParentCommentId(String parentCommentId);
    Optional<Comment> findById(String id);
} 