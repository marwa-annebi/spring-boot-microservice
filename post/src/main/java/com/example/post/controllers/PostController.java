package com.example.post.controllers;

import com.example.post.dtos.CreateDto;
import com.example.post.dtos.UpdatePostDto;
import com.example.post.dtos.WithLikesCount;
import com.example.post.models.Post;
import com.example.post.models.User;
import com.example.post.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    private final PostService postService;

    @GetMapping("/all")
    public ResponseEntity<
            PageImpl
                    <WithLikesCount>> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchTerm) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));

        // Get the current user
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Fetch posts excluding those created by the current user

        PageImpl<WithLikesCount> posts = postService.searchPosts(currentUser.getId(), searchTerm, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/my-posts")
    public ResponseEntity<Page<Post>> getMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));

        // Récupérer l'utilisateur authentifié
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized si utilisateur non authentifié
        }

        // Récupérer les posts de l'utilisateur actuel
        Page<Post> myPosts = postService.findPostsByCurrentUser(currentUser.getId(), pageable);
        return ResponseEntity.ok(myPosts);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return (User) authentication.getPrincipal();
        }
        return null; // Retourne null si aucun utilisateur n'est authentifié
    }
    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@ModelAttribute @Valid CreateDto CreateDto,
                                           @RequestParam("images") List<MultipartFile> images) throws IOException {
        Post createdPost = postService.createPost(CreateDto, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable String id) {
        System.out.println("posttttttttt");
        Optional<Post> post = postService.getPostById(id);
        System.out.println("ressss"+post);
        return post.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PatchMapping("/edit/{id}")
    public ResponseEntity<Post> editPost(
            @PathVariable String id,
            @ModelAttribute @Valid UpdatePostDto updatePostDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        try {
            // Pass images (MultipartFile list) to the service
            Post updatedPost = postService.editPost(id, updatePostDto, images);

            // Return the updated post
            return ResponseEntity.ok(updatedPost);
        } catch (IOException e) {
            // Log the exception for debugging
            System.err.println("Error while saving images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Return an appropriate response if image saving fails
        } catch (Exception e) {
            // Handle other errors
            System.err.println("Error while editing post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    @GetMapping("/details/{postId}")
    public com.example.post.dto.PostResponseDto getPostWithDetails(@PathVariable String postId) {
        return postService.getPostWithDetails(postId);
    }

}
