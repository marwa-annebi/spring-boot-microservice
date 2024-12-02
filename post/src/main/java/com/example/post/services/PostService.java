package com.example.post.services;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserClient userClient;
    private final MongoTemplate mongoTemplate;
    private final ImageService imageService;

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
        // Extract user ID from the authenticated JWT token
        // Get authenticated user
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("userDetails: " + userDetails);

        // Fetch user by email
        User user = userClient.findByEmail(userDetails.getEmail());
        System.out.println("user: " + user);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        // Use the user ID to filter posts
        Criteria criteria = Criteria.where("postedBy").is(new ObjectId(user.getId().toString()));

        // MongoDB aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria), // Filter posts by user ID
                Aggregation.lookup("likes", "_id", "post", "likes"), // Join with 'likes' collection
                Aggregation.project("_id", "description", "images", "postedBy", "comments", "createdAt", "updatedAt")
                        .and(ArrayOperators.Size.lengthOfArray("likes")).as("likesCount") // Count likes
                        .and(ConditionalOperators.when(
                                        ComparisonOperators.valueOf("likes.likedBy").equalToValue(new ObjectId(user.getId().toString())))
                                .then(true)
                                .otherwise(false)).as("liked"), // Check if user liked the post
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")), // Sort by creation date
                Aggregation.skip(pageable.getOffset()), // Pagination
                Aggregation.limit(pageable.getPageSize())
        );

        // Execute aggregation query
        List<WithLikesCount> posts = mongoTemplate.aggregate(aggregation, "posts", WithLikesCount.class).getMappedResults();

        long count = getCountOfMatchedDocuments("", true);

        return new PageImpl<>(posts, pageable, count);
    }

}
