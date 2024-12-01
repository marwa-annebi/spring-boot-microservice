package com.example.Comment.Service.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentDto {
    private String postId;
    private String userId;
    private String text;
}