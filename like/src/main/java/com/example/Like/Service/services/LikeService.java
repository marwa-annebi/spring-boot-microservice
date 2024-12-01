package com.example.Like.Service.services;

import com.example.Like.Service.dtos.CreateLikeDto;
import com.example.Like.Service.models.Like;
import com.example.Like.Service.repositories.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public Like addLike(CreateLikeDto createLikeDto) {
        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(
                createLikeDto.getPostId(), createLikeDto.getUserId());

        if (existingLike.isEmpty()) {
            Like like = Like.builder()
                    .postId(createLikeDto.getPostId())
                    .userId(createLikeDto.getUserId())
                    .build();
            return likeRepository.save(like);
        } else {
            // Le like existe déjà
            return existingLike.get();
        }
    }

    public void removeLike(CreateLikeDto createLikeDto) {
        likeRepository.deleteByPostIdAndUserId(
                createLikeDto.getPostId(), createLikeDto.getUserId());
    }

    public long countLikesByPostId(String postId) {
        return likeRepository.countByPostId(postId);
    }
}