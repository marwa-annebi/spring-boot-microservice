package com.example.Comment.Service.services;

import com.example.Comment.Service.client.PostClient;
import com.example.Comment.Service.client.UserClient;
import com.example.Comment.Service.dtos.*;
import com.example.Comment.Service.models.Comment;
import com.example.Comment.Service.models.Post;
import com.example.Comment.Service.models.User;
import com.example.Comment.Service.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    private final CommentRepository commentRepository;
    private final PostClient postClient;
    private final MongoTemplate mongoTemplate;
    private  final UserClient userClient;


    public Comment addComment(CreateCommentDto createCommentDto) {
        // Get current authenticated user
        User user = getCurrentUser();

        // Create a new comment object
        Comment comment = Comment.builder()
                .text(createCommentDto.getText())
                .postId(new Post(createCommentDto.getPostId()))
                .userId(user)
                .build();

        // Save the comment
        return commentRepository.save(comment);
    }

//    public List<Comment> getCommentsByPostId(String postId) {
//        return commentRepository.findByPostId(postId);
//    }
//
//    public long countCommentsByPostId(String postId) {
//        return commentRepository.countByPostId(postId);
//    }
public List<CommentWithUserDetails> getCommentsWithUserDetails(String postId) {
    // Step 1: Fetch comments by postId with replies
    List<CommentDto> comments = getCommentsWithReplies(postId);

    // Step 2: Extract user IDs from comments and replies
    List<String> userIds = comments.stream()
            .flatMap(comment -> extractUserIdsFromComment(comment).stream())
            .distinct()
            .collect(Collectors.toList());

    // Step 3: Fetch user details from user-service
    List<User> userDetails = userClient.getUserDetails(userIds);

    // Step 4: Map user details by userId for quick lookup
    Map<String, User> userDetailsMap = userDetails.stream()
            .collect(Collectors.toMap(User::getId, user -> user));

    // Step 5: Enrich comments with user details and replies
    return comments.stream()
            .map(comment -> enrichCommentWithUserDetails(comment, userDetailsMap))
            .collect(Collectors.toList());
}

    private List<CommentDto> getCommentsWithReplies(String postId) {
        ObjectId objectId = new ObjectId(postId);
        Aggregation aggregation = Aggregation.newAggregation(
                // Match comments by postId
                Aggregation.match(Criteria.where("postId").is(objectId)),

                // Lookup for replies
                Aggregation.lookup("comments", "_id", "parentCommentId", "replies"),

                // Project the required fields
                Aggregation.project("text", "userId", "postId", "_id", "replies", "createdAt")
                        .and("_id").as("id")
        );

        return mongoTemplate.aggregate(aggregation, "comments", CommentDto.class)
                .getMappedResults();
    }

    private List<String> extractUserIdsFromComment(CommentDto comment) {
        List<String> userIds = new ArrayList<>();
        userIds.add(comment.getUserId());
        if (comment.getReplies() != null) {
            for (CommentDto reply : comment.getReplies()) {
                userIds.addAll(extractUserIdsFromComment(reply));
            }
        }
        return userIds;
    }

    private CommentWithUserDetails enrichCommentWithUserDetails(CommentDto comment, Map<String, User> userDetailsMap) {
        User userDetails = userDetailsMap.get(comment.getUserId());
        List<CommentWithUserDetails> enrichedReplies = new ArrayList<>();

        if (comment.getReplies() != null) {
            enrichedReplies = comment.getReplies().stream()
                    .map(reply -> enrichCommentWithUserDetails(reply, userDetailsMap))
                    .collect(Collectors.toList());
        }

        return new CommentWithUserDetails(comment, userDetails, enrichedReplies);
    }
    public Map<String, Long> getCommentCountsByPosts(List<String> postIds) {
        // Match comments with the given post IDs
        AggregationOperation matchOperation = Aggregation.match(
                Criteria.where("postId").in(postIds)
        );

        // Group by postId and count the number of comments
        AggregationOperation groupOperation = Aggregation.group("postId").count().as("commentCount");

        // Project the results into a map format
        AggregationOperation projectOperation = Aggregation.project()
                .and("_id").as("postId")
                .and("commentCount").as("count");

        // Build the aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                groupOperation,
                projectOperation
        );

        // Execute the aggregation
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "comments", Map.class);

        // Convert results to a Map of postId -> commentCount
        return results.getMappedResults().stream()
                .collect(Collectors.toMap(
                        result -> result.get("postId").toString(),
                        result -> ((Number) result.get("count")).longValue()
                ));
    }
    public Map<String, List<Comment>> getCommentsForPosts(List<String> postIds) {
        // Create a query to fetch comments where postId is in the list of postIds
        Query query = new Query(Criteria.where("postId").in(postIds));

        // Fetch comments matching the query
        List<Comment> comments = mongoTemplate.find(query, Comment.class);

        // Group comments by postId and return the result
        return comments.stream().collect(Collectors.groupingBy(comment -> comment.getPostId().getId()));
    }
//    public Map<String, Long> getCommentCounts(List<String> postIds) {
//        return postIds.stream()
//                .collect(Collectors.toMap(
//                        postId -> postId,
//                        postId -> commentRepository.countByPostId(postId)
//                ));
//    }
private ObjectId safeConvertToObjectId(String id) {
    try {
        return new ObjectId(id); // Attempt to convert the string to ObjectId
    } catch (IllegalArgumentException e) {
        // Log the error for debugging (optional)
        System.err.println("Invalid ObjectId: " + id);
        return null; // Return null for invalid ObjectId
    }
}

    public Map<String, Long> getCommentCounts(List<String> postIds) {
        // Convert string postIds to ObjectId instances
        List<ObjectId> objectIds = postIds.stream()
                .map(this::convertToObjectId)
                .filter(Objects::nonNull) // Exclude nulls for invalid IDs
                .collect(Collectors.toList());

        // Define the aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                // Match comments for the provided postIds
                Aggregation.match(Criteria.where("postId").in(objectIds)),

                // Perform a lookup to join comments with the posts collection in another database
                Aggregation.lookup("postdb.posts", "_id", "_id", "postDetails"),

                // Unwind the postDetails array (if needed)
                Aggregation.unwind("postDetails", true) // true = preserve nulls
        );

        // Execute the aggregation pipeline
        List<CommentWithPostDetails> results = mongoTemplate.aggregate(
                aggregation, "comments", CommentWithPostDetails.class).getMappedResults();

        // Group the results by postId and count the number of comments
        Map<String, Long> commentCounts = results.stream()
                .filter(comment -> comment.getPostId() != null) // Ensure postId is not null
                .collect(Collectors.groupingBy(
                        comment -> comment.getPostId().toString(), // Convert ObjectId to String
                        Collectors.counting()
                ));

        // Include all requested postIds in the result, even if they have no comments
        postIds.forEach(postId -> commentCounts.putIfAbsent(postId, 0L));

        return commentCounts;
    }

    private ObjectId convertToObjectId(String id) {
        try {
            return new ObjectId(id); // Attempt to convert the string to ObjectId
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid ObjectId format: " + id);
            return null; // Return null for invalid ObjectId strings
        }
    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return (User) authentication.getPrincipal(); // Check if principal is correctly set
        }
        throw new RuntimeException("Unauthorized: No authenticated user found");
    }
    public Comment addReply(CreateReplyDto createReplyDto) {
        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Fetch the parent comment
        Optional<Comment> parentCommentOpt = commentRepository.findById(createReplyDto.getParentCommentId());
        System.out.println(parentCommentOpt);
        if (parentCommentOpt.isEmpty()) {
            throw new RuntimeException("Parent comment not found.");
        }
        Comment parentComment = parentCommentOpt.get();

        // Create a new reply
        Comment reply = Comment.builder()
                .text(createReplyDto.getReplyText())
                .postId(parentComment.getPostId())
                .userId(currentUser)
                .parentCommentId(parentComment)
                .build();

        // Save the reply
        Comment savedReply = commentRepository.save(reply);

        // Use MongoTemplate to update the parent comment's replies field
        Query query = new Query(Criteria.where("_id").is(parentComment.getId()));
        Update update = new Update().push("replies", savedReply.getId()); // Add the reply's ID to the replies list
        mongoTemplate.updateFirst(query, update, Comment.class);

        return savedReply;
    }
//    public Map<String, Long> getLikeDislikeCounts(String postId) {
//        Post post = new Post(postId);
//
//        // Count likes
//        long likeCount = likeRepository.countByPostAndTop(post, true);
//
//        // Count dislikes
//        long dislikeCount = likeRepository.countByPostAndTop(post, false);
//
//        // Return the counts
//        return Map.of(
//                "likes", likeCount,
//                "dislikes", dislikeCount
//        );
//    }
//
//    public Map<String, Object> getPostReactionsAndComments(String postId) {
//        // Count likes and dislikes
//        Map<String, Long> reactions = getLikeDislikeCounts(postId);
//
//        // Count comments
//        long commentCount = countCommentsByPostId(postId);
//
//        // Fetch comments
//        List<Comment> comments = getCommentsByPostId(postId);
//
//        // Aggregate the response
//        return Map.of(
//                "likes", reactions.get("likes"),
//                "dislikes", reactions.get("dislikes"),
//                "commentsCount", commentCount,
//                "comments", comments
//        );
//    }

}