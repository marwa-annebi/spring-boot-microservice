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

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private  final MongoTemplate mongoTemplate;
    private  final UserClient userClient;
    private  final PostClient postClient;
    public Like addLike(CreateLikeDto createLikeDto) throws ChangeSetPersister.NotFoundException {
        // Fetch user details from the token
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);
        // Fetch user and post using Feign clients
        User user = userClient.findByEmail(userDetails.getEmail());
        Post post = postClient.getPostById(createLikeDto.getPostId()).getBody();

        if (post == null) {
            throw new ChangeSetPersister.NotFoundException();
        }

        // Create and save the Like
        Like like = Like.builder()
                .likedBy(user)
                .post(post)
                .top(true)
                .build();

        return likeRepository.save(like);
    }

    public void removeLike(CreateLikeDto createLikeDto) throws ChangeSetPersister.NotFoundException {
        // Fetch user details from the token
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);
        // Fetch user and post using Feign clients
        User user = userClient.findByEmail(userDetails.getEmail());
        Post post = postClient.getPostById(createLikeDto.getPostId()).getBody();
        Query query = new Query();
        query.addCriteria(Criteria.where("likedBy").is(new ObjectId(user.getId())).and("post").is(new ObjectId(post.getId())));
        Like existingLike = mongoTemplate.findOne(query,Like.class);

        if (existingLike != null) {
            likeRepository.delete(existingLike);
           /* mongoTemplate.update(Post.class)
                    .matching(query(where("_id").is(post.getId())))
                    .apply(new Update().pull("likes", Query.query(where("_id").is(existingLike.getId()))))
                    .first();*/
        } else {
            Like dislike = Like.builder()
                    .likedBy(user)
                    .post(post)
                    .top(false)
                    .build();
            Like savedLike = likeRepository.save(dislike);
           /* mongoTemplate.update(Post.class)
                    .matching(query(where("_id").is(post.getId())))
                    .apply(new Update().addToSet("likes", savedLike))
                    .first();*/
        }
    }

}