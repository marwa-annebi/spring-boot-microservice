package com.example.Like.Service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateLikeDto {
    @NotBlank(message = "Post ID is mandatory")
    private String postId;
}