//package com.example.Comment.Service.controllers;
//
//import com.example.Comment.Service.models.Post;
//import com.example.Comment.Service.services.PostService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/posts")
//@RequiredArgsConstructor
//public class PostController {
//
//    private final PostService postService;
//
//    @GetMapping("/{postId}/comments")
//    public ResponseEntity<Post> getPostWithComments(@PathVariable String postId) {
//        Post postWithComments = postService.getPostWithComments(postId);
//        return ResponseEntity.ok(postWithComments);
//    }
//}
