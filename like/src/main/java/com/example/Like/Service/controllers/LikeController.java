package com.example.Like.Service.controllers;

import com.example.Like.Service.dtos.CreateLikeDto;
import com.example.Like.Service.dtos.ErrorDto;
import com.example.Like.Service.models.Like;
import com.example.Like.Service.services.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/like")
public class LikeController {
    private final LikeService likeService;


@PostMapping("/add")
public ResponseEntity<?> addLike(@RequestBody @Valid CreateLikeDto createLikeDto) {
    System.out.println("Received payload: " + createLikeDto);
    try {
        Like like = likeService.addLike(createLikeDto);
        return ResponseEntity.status(201).body(like);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorDto(e.getMessage()));
    }
}


    @PostMapping("/dislike")
    public ResponseEntity<?> addDislike(@RequestBody CreateLikeDto createLikeDto) {
        System.out.println("Received payload: " + createLikeDto);
        try {
            Like dislike = likeService.addDislike(createLikeDto);
            return ResponseEntity.status(201).body(dislike);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorDto(e.getMessage()));
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<Void> removeLikeOrDislike(@RequestBody CreateLikeDto createLikeDto) {
        likeService.removeLikeOrDislike(createLikeDto);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/count")
    public ResponseEntity<Map<String, Long>> getLikesCount(@RequestBody List<String> postIds) {
        return ResponseEntity.ok(likeService.getLikesCount(postIds));
    }

    @PostMapping("/dislike/count")
    public ResponseEntity<Map<String, Long>> getDislikesCount(@RequestBody List<String> postIds) {
        return ResponseEntity.ok(likeService.getDislikesCount(postIds));
    }


    @PostMapping("/user")
    public ResponseEntity<Map<String, Boolean>> getUserLikes(@RequestBody List<String> postIds) {
        return ResponseEntity.ok(likeService.getUserLikes(postIds));
    }

@PostMapping("/user/post")
public ResponseEntity<Map<String, Boolean>> getCurrentUserPostReaction(@RequestBody String postId) {
    return ResponseEntity.ok(likeService.getCurrentUserPostReaction(postId));
}
    @PostMapping("/user/dislikes")
    public ResponseEntity<Map<String, Boolean>> getUserDislikes(@RequestBody List<String> postIds) {
        return ResponseEntity.ok(likeService.getUserDislikes(postIds));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Like>> getLikeDetails(@PathVariable String postId) {
        List<Like> likes = likeService.getLikeDetails(postId);
        return ResponseEntity.ok(likes);
    }
    @GetMapping("/{postId}/reactions")
    public ResponseEntity<Map<String, Long>> getLikeDislikeCounts(@PathVariable String postId) {
        Map<String, Long> counts = likeService.getLikeDislikeCounts(postId);
        return ResponseEntity.ok(counts);
    }

}
//    @PostMapping("/count")
//    public ResponseEntity<Map<String, Long>> getLikesCount(@RequestBody List<String> postIds) {
//        return ResponseEntity.ok(likeService.getLikesCount(postIds));
//    }

//    @PostMapping("/count/likes-dislikes")
//    public ResponseEntity<Map<String, Map<String, Long>>> getLikesAndDislikesCount(@RequestBody List<String> postIds) {
//        return ResponseEntity.ok(likeService.getLikesAndDislikesCount(postIds));
//    }

//    @PostMapping("/add")
//    public ResponseEntity<?> addLike(@RequestBody @Valid CreateLikeDto createLikeDto) {
//        try {
//            Like like = likeService.addLike(createLikeDto);
//            return ResponseEntity.status(201).body(like);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(new ErrorDto(e.getMessage()));
//        }
//    }