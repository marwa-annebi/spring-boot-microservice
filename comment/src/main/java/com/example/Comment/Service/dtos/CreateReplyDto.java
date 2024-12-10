package com.example.Comment.Service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateReplyDto {
    @NotBlank(message = "Reply text is required.")
    private String replyText;

    @NotBlank(message = "Post ID is required.")
    private String postId;

    @NotBlank(message = "Parent comment ID is required.")
    private String parentCommentId;
}
