package com.example.Like.Service.controllers;

import com.example.Like.Service.dtos.CreateLikeDto;
import com.example.Like.Service.models.Like;
import com.example.Like.Service.services.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService likeService;

    /**
     * Endpoint to add a like to a post.
     *
     * @param createLikeDto DTO containing post ID and user details.
     * @return ResponseEntity with the created Like object.
     */
    @PostMapping("/add")
    public ResponseEntity<Like> addLike(@RequestBody CreateLikeDto createLikeDto) throws ChangeSetPersister.NotFoundException {
        Like like = likeService.addLike(createLikeDto);
        return ResponseEntity.status(201).body(like);
    }

    /**
     * Endpoint to add a dislike to a post.
     *
     * @param createLikeDto DTO containing post ID and user details.
     * @return ResponseEntity with the created Dislike object.
     */
    @PostMapping("/dislike")
    public ResponseEntity<Like> addDislike(@RequestBody CreateLikeDto createLikeDto) throws ChangeSetPersister.NotFoundException {
        Like dislike = likeService.addDislike(createLikeDto);
        return ResponseEntity.status(201).body(dislike);
    }

    /**
     * Endpoint to remove a like or dislike from a post.
     *
     * @param createLikeDto DTO containing post ID and user details.
     * @return ResponseEntity indicating the operation's success.
     */
    @PostMapping("/remove")
    public ResponseEntity<Void> removeLikeOrDislike(@RequestBody CreateLikeDto createLikeDto) throws ChangeSetPersister.NotFoundException {
        likeService.addDislike(createLikeDto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to get the count of likes for a list of posts.
     *
     * @param postIds List of post IDs.
     * @return ResponseEntity with a map of post IDs and their respective like counts.
     */
    @PostMapping("/count")
    public ResponseEntity<Map<String, Long>> getLikesCount(@RequestBody List<String> postIds) {
        Map<String, Long> likesCount = likeService.getLikesCount(postIds);
        return ResponseEntity.ok(likesCount);
    }

    @PostMapping("/dislike/count")
    public ResponseEntity<Map<String, Long>> getDiLikesCount(@RequestBody List<String> postIds) {
        Map<String, Long> likesCount = likeService.getDisLikesCount(postIds);
        return ResponseEntity.ok(likesCount);
    }

    /**
     * Endpoint to check if a user liked specific posts.
     *
     * @param postIds List of post IDs.
     * @return ResponseEntity with a map of post IDs and a boolean indicating if the user liked each post.
     */
    @PostMapping("/user")
    public ResponseEntity<Map<String, Boolean>> getUserLikes(
            @RequestBody List<String> postIds
    ) {
        Map<String, Boolean> userLikes = likeService.getUserLikes(postIds);
        return ResponseEntity.ok(userLikes);
    }

    /**
     * Endpoint to check if a user disliked specific posts.
     *
     * @param postIds List of post IDs.
     * @return ResponseEntity with a map of post IDs and a boolean indicating if the user disliked each post.
     */
    @PostMapping("/user/dislikes")
    public ResponseEntity<Map<String, Boolean>> getUserDislikes(
            @RequestBody List<String> postIds
    ) {
        Map<String, Boolean> userDislikes = likeService.getUserDislikes(postIds);
        return ResponseEntity.ok(userDislikes);
    }
}
