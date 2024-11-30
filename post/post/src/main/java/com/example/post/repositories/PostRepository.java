package com.example.post.repositories;

import com.example.post.models.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
public interface PostRepository extends MongoRepository<Post, String> {
}
