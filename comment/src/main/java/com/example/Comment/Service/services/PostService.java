//package com.example.Comment.Service.services;
//
//import com.example.Comment.Service.client.PostClient;
//import com.example.Comment.Service.models.Comment;
//import com.example.Comment.Service.models.Post;
//import com.example.Comment.Service.repositories.CommentRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class PostService {
//
//    private final PostClient postClient;
//    private final CommentRepository commentRepository;
//
//    public Post getPostWithComments(String postId) {
//        // Fetch the post using Feign client
//        Post post = postClient.getPostById(postId);
//        if (post == null) {
//            throw new IllegalArgumentException("Post not found for ID: " + postId);
//        }
//
//        // Fetch comments associated with the post
//        List<Comment> comments = commentRepository.findByPostId(postId);
//        post.setComments(comments);
//
//        return post;
//    }
//    public PostResponseDto getPostWithDetails(String postId) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//
//        // Fetch comment count
//        long commentCount = commentRepository.countByPostId(postId);
//
//        return PostResponseDto.builder()
//                .id(post.getId())
//                .description(post.getDescription())
//                .images(post.getImages())
//                .postedBy(post.getPostedBy())
//                .likesCount(post.getLikesCount())
//                .commentsCount(commentCount) // Add this field
//                .build();
//    }
//
//}
