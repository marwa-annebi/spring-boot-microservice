package com.example.Like.Service.services;

import com.example.Like.Service.client.PostClient;
import com.example.Like.Service.client.UserClient;
import com.example.Like.Service.dtos.CreateLikeDto;
import com.example.Like.Service.models.Like;
import com.example.Like.Service.models.Post;
import com.example.Like.Service.models.User;
import com.example.Like.Service.repositories.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostClient postClient; // Feign client to interact with Post Microservice
    private final UserClient userClient; // Feign client to interact with User Microservice
    private final MongoTemplate mongoTemplate;
    /**
     * Add a like to a post.
     */
    public Like addLike(CreateLikeDto createLikeDto) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);
        // Fetch user and post using Feign clients
        User user = userClient.findByEmail(userDetails.getEmail());
        Post post = postClient.getPostById(createLikeDto.getPostId());

        // Check if the like already exists
        Optional<Like> existingLike = likeRepository.findByLikedByAndPost(user, post);
        if (existingLike.isPresent() && existingLike.get().isTop()) {
            throw new IllegalArgumentException("User already liked this post.");
        }

        // Create and save the Like
        Like like = Like.builder()
                .likedBy(user)
                .post(post)
                .top(true)
                .build();
        return likeRepository.save(like);
    }

    /**
     * Add a dislike to a post.
     */
    public Like addDislike(CreateLikeDto createLikeDto) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);
        // Fetch user and post using Feign clients
        User user = userClient.findByEmail(userDetails.getEmail());
        Post post = postClient.getPostById(createLikeDto.getPostId());

        // Check if the dislike already exists
        Optional<Like> existingDislike = likeRepository.findByLikedByAndPost(user, post);
        if (existingDislike.isPresent() && !existingDislike.get().isTop()) {
            throw new IllegalArgumentException("User already disliked this post.");
        }

        // Create and save the Dislike
        Like dislike = Like.builder()
                .likedBy(user)
                .post(post)
                .top(false)
                .build();
        return likeRepository.save(dislike);
    }

    /**
     * Remove a like or dislike from a post.
     */
    public void removeLikeOrDislike(CreateLikeDto createLikeDto) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);

        User user = userClient.findByEmail(userDetails.getEmail());
        Post post = postClient.getPostById(createLikeDto.getPostId());

        // Build query to find existing like/dislike
        Query query = new Query(Criteria.where("likedBy").is(user.getId()).and("post").is(post.getId()));
        Like existingLike = mongoTemplate.findOne(query, Like.class);

        // Remove the like/dislike if it exists
        if (existingLike != null) {
            mongoTemplate.remove(query, Like.class);
        }
    }

    /**
     * Get likes count for multiple posts.
     */
    public Map<String, Long> getLikesCount(List<String> postIds) {

        System.out.println("hello"+postIds);
        return postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        postId -> {
                            Query query = new Query(Criteria.where("post").is(new ObjectId(postId)).and("top").is(true));
                            return mongoTemplate.count(query, Like.class);
                        }
                ));
    }

    public Map<String, Long> getDisLikesCount(List<String> postIds) {

        System.out.println("hello"+postIds);
        return postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        postId -> {
                            Query query = new Query(Criteria.where("post").is(postId).and("top").is(false));
                            return mongoTemplate.count(query, Like.class);
                        }
                ));
    }

    /**
     * Check if a user liked specific posts.
     */
    public Map<String, Boolean> getUserLikes(List<String> postIds) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userClient.findByEmail(userDetails.getEmail());
        return postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        postId -> {
                            Query query = new Query(Criteria.where("likedBy").is(user.getId()).and("post").is(postId).and("top").is(true));
                            return mongoTemplate.exists(query, Like.class);
                        }
                ));
    }

    public Map<String, Boolean> getUserDislikes(List<String> postIds) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);

        User user = userClient.findByEmail(userDetails.getEmail());
        return postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        postId -> {
                            Query query = new Query(Criteria.where("likedBy").is(user.getId()).and("post").is(postId).and("top").is(false));
                            return mongoTemplate.exists(query, Like.class);
                        }
                ));
    }

}