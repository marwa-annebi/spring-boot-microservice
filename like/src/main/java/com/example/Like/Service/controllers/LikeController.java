package com.example.Like.Service.controllers;

import com.example.Like.Service.dtos.CreateLikeDto;
import com.example.Like.Service.models.Like;
import com.example.Like.Service.services.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/add")
    public ResponseEntity<Like> addLike(@RequestBody CreateLikeDto createLikeDto) throws ChangeSetPersister.NotFoundException {
        Like like = likeService.addLike(createLikeDto);
        return ResponseEntity.status(201).body(like);
    }

    @PostMapping("/remove")
    public ResponseEntity<Void> removeLike(@RequestBody CreateLikeDto createLikeDto) throws ChangeSetPersister.NotFoundException {
        likeService.removeLike(createLikeDto);
        return ResponseEntity.noContent().build();
    }


}
