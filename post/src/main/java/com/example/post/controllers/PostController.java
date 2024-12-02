package com.example.post.controllers;


import com.example.post.dtos.CreateDto;
import com.example.post.dtos.WithLikesCount;
import com.example.post.models.Post;
import com.example.post.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/post")
@CrossOrigin
public class PostController {

    private  final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@ModelAttribute @Valid CreateDto CreateDto,
                                           @RequestParam("images") List<MultipartFile> images) throws IOException {
        Post createdPost = postService.create(CreateDto, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable String id) {
        System.out.println("posttttttttt");
        Optional<Post> post = postService.getPostById(id);
        System.out.println("ressss"+post);
        return post.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePostById(@PathVariable String id) {
        postService.deletePostById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<WithLikesCount>> getAllPostsByPageAndUsername(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (page < 1) {
                page = 1; // Ensure page number is at least 1
            }

            Pageable pageable = PageRequest.of(page-1, size);
            Page<WithLikesCount> posts = postService.findAllByPageAndUsername(searchTerm, pageable);
            System.out.println(posts);
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            System.out.println("An error occurred while processing the request"+ e);
            // Return an appropriate error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/myPosts")
    public ResponseEntity<Page<WithLikesCount>> myPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 1) {
            page = 1; // Ensure page number is at least 1
        }

        Pageable pageable = PageRequest.of(page-1, size);
        Page<WithLikesCount> posts = postService.myPosts(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }



}
