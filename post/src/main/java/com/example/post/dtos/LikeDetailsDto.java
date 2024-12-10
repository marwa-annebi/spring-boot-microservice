package com.example.post.dtos;

import lombok.Data;

@Data
public class LikeDetailsDto {
    private String id;
    private String postId;
    private String userId;
    private String createdAt;
}
