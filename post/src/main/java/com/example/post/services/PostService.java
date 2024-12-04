package com.example.post.services;

import com.example.post.client.LikeClient;
import com.example.post.client.UserClient;
import com.example.post.config.SecurityUtils;
import com.example.post.dtos.CreateDto;
import com.example.post.dtos.UserDto;
import com.example.post.dtos.WithLikesCount;
import com.example.post.models.Post;
import com.example.post.models.User;
import com.example.post.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserClient userClient;
    private final MongoTemplate mongoTemplate;
    private final ImageService imageService;
    private final LikeClient likeClient; // Inject LikeClient
    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";

    // Helper method to map CreateDto to Post
    private Post createToPost(CreateDto createPostDto) {
        Post post = new Post();
        post.setDescription(createPostDto.getDescription());
        return post;
    }

    public Post create(CreateDto createPostDto, List<MultipartFile> images) throws IOException {
        // Get authenticated user
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);

        // Fetch user by email
        User user = userClient.findByEmail(userDetails.getEmail());
        System.out.println("user: " + user);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        Post post = createToPost(createPostDto);
        post.setPostedBy(user);

        // Save images
        List<String> imagePaths = imageService.saveImages(images);
        post.setImages(imagePaths);

        // Save the post to MongoDB
        Post savedPost = postRepository.save(post);

        // Update user's post list in MongoDB
        mongoTemplate.update(User.class)
                .matching(query(Criteria.where("_id").is(user.getId())))
                .apply(new Update().push("posts", new ObjectId(savedPost.getId())))
                .first();

        return savedPost;
    }

    public Optional<Post> getPostById(String id) {
        return postRepository.findById(id);
    }

    public void deletePostById(String id) {
        postRepository.deleteById(id);
    }

    public Page<WithLikesCount> findAllByPageAndUsername(String searchTerm, Pageable pageable) {
        List<AggregationOperation> operations = new ArrayList<>();

        // Get authenticated user
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);

        // Fetch user by email
        User user = userClient.findByEmail(userDetails.getEmail());
        System.out.println("user: " + user);

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }



        // Exclude posts by the same user
        Criteria matchCriteria = Criteria.where("postedBy").ne(new ObjectId(user.getId().toString()));
        AggregationOperation matchOperation = Aggregation.match(matchCriteria);
        operations.add(matchOperation);

        // Add search term filter
        if (StringUtils.hasText(searchTerm)) {
            Criteria searchCriteria = Criteria.where("description")
                    .regex(Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE));
            operations.add(Aggregation.match(searchCriteria));
        }

        // Count likes for each post


        // Sort and paginate
        operations.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")));
        operations.add(Aggregation.skip(pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize()));

        // Execute aggregation
        Aggregation aggregation = Aggregation.newAggregation(operations);
        List<WithLikesCount> results = mongoTemplate.aggregate(aggregation, "posts", WithLikesCount.class).getMappedResults();

        // Count documents
        long count = getCountOfMatchedDocuments(searchTerm, false);

        return new PageImpl<>(results, pageable, count);
    }

    private long getCountOfMatchedDocuments(String searchTerm, boolean myPosts) {
        // Get authenticated user
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);

        // Fetch user by email
        User user = userClient.findByEmail(userDetails.getEmail());
        System.out.println("user: " + user);

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (myPosts) {
            Criteria criteria = Criteria.where("postedBy").is(new ObjectId(user.getId()));
            return mongoTemplate.count(Query.query(criteria), "posts");
        } else if (StringUtils.isEmpty(searchTerm)) {
            return mongoTemplate.count(new Query(), "posts");
        } else {
            LookupOperation lookupOperation = Aggregation.lookup("users", "postedBy", "_id", "user");
            MatchOperation matchOperation = Aggregation.match(Criteria.where("user.username").regex(Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE)));
            Aggregation aggregation = Aggregation.newAggregation(lookupOperation, matchOperation);
            return mongoTemplate.aggregate(aggregation, "posts", Map.class).getMappedResults().size();
        }
    }

    public Page<WithLikesCount> myPosts(Pageable pageable) {
        // Get authenticated user
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userClient.findByEmail(userDetails.getEmail());

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        // Fetch posts for the user
        Criteria criteria = Criteria.where("postedBy").is(new ObjectId(user.getId().toString()));
        Query query = new Query(criteria).with(pageable);
        List<Post> posts = mongoTemplate.find(query, Post.class, "posts");

        // Fetch post IDs
        List<String> postIds = posts.stream()
                .map(post -> post.getId()) // Explicitly convert ObjectId to String
                .collect(Collectors.toList());

System.out.println(postIds);
        // Fetch likes, dislikes, and user-specific interactions using LikeClient
        Map<String, Integer> likesCount = likeClient.getLikesCount(postIds);
        Map<String, Integer> dislikesCount = likeClient.getDislikesCount(postIds);
        Map<String, Boolean> likedByUser = likeClient.getUserLikes(postIds);
        Map<String, Boolean> dislikedByUser = likeClient.getUserDislikes(postIds);

        // Aggregate data into DTO
        List<WithLikesCount> enrichedPosts = posts.stream().map(post -> {
            WithLikesCount enrichedPost = new WithLikesCount();
            enrichedPost.setId(post.getId().toString());
            enrichedPost.setDescription(post.getDescription());
            enrichedPost.setImages(post.getImages());
            enrichedPost.setPostedBy(post.getPostedBy().toString());
            enrichedPost.setCreatedAt(post.getCreatedAt());
            enrichedPost.setUpdatedAt(post.getUpdatedAt());
            enrichedPost.setLikesCount(likesCount.getOrDefault(post.getId().toString(), 0));
            enrichedPost.setDisliked(dislikesCount.getOrDefault(post.getId().toString(), 0) > 0);
            enrichedPost.setLiked(likedByUser.getOrDefault(post.getId().toString(), false));
            enrichedPost.setDisliked(dislikedByUser.getOrDefault(post.getId().toString(), false));
            return enrichedPost;
        }).collect(Collectors.toList());

        long totalPosts = mongoTemplate.count(query.skip(0).limit(0), "posts");
        return new PageImpl<>(enrichedPosts, pageable, totalPosts);
    }
}
