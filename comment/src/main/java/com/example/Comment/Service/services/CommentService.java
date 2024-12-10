package com.example.Comment.Service.services;

import com.example.Comment.Service.client.PostClient;
import com.example.Comment.Service.client.UserClient;
import com.example.Comment.Service.dtos.CreateCommentDto;
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    private final CommentRepository commentRepository;
    private final PostClient postClient;
    private final MongoTemplate mongoTemplate;


    public Comment addComment(CreateCommentDto createCommentDto) {
        // Get current authenticated user
        User user = getCurrentUser();

        // Create a new comment object
        Comment comment = Comment.builder()
                .text(createCommentDto.getText())
                .postId(new Post(createCommentDto.getPostId()))
                .userId(user)
                .parentCommentId(createCommentDto.getParentCommentId())
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
public List<Comment> getCommentsByPostId(String postId) {
    // Query by the Post object instead of the string ID
    Post post = new Post(postId);
    return commentRepository.findByPostId(post);
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

    public Map<String, Long> getCommentCounts(List<String> postIds) {
        // Build the query to filter comments by the given postIds
        Query query = new Query(Criteria.where("postId").in(postIds));
        List<Comment> comments = mongoTemplate.find(query, Comment.class);

        // Group comments by postId and count them
        Map<String, Long> commentCounts = comments.stream()
                .filter(comment -> comment.getPostId() != null) // Ensure postId is not null
                .collect(Collectors.groupingBy(
                        comment -> comment.getPostId().toString(), // Convert ObjectId to String
                        Collectors.counting()
                ));

        // Include all requested postIds in the result, even if they have no comments
        postIds.forEach(postId -> commentCounts.putIfAbsent(postId, 0L));

        return commentCounts;
    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return (User) authentication.getPrincipal(); // Check if principal is correctly set
        }
        throw new RuntimeException("Unauthorized: No authenticated user found");
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