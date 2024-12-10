package com.example.post.repositories;

import com.example.post.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    Page<Post> findAll(Pageable pageable);
//
    Page<Post> findByPostedBy_Id(String userId, Pageable pageable);


}
//    Page<Post> findByPostedBy_IdNot(String userId, Pageable pageable);
//
//    // Fetch posts by other users (excluding a specific user by ID) and filter by description
//    Page<Post> findByPostedBy_IdNotAndDescriptionContainingIgnoreCase(String userId, String description, Pageable pageable);

//    // Recherche par description
//    Page<Post> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
//
//    // Recherche par utilisateur (email dans `postedBy`)
//    List<Post> findByPostedBy_Email(String email);