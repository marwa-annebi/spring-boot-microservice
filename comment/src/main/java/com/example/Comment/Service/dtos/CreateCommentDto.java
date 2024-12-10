
package com.example.Comment.Service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentDto {
    @NotBlank(message = "Post ID is mandatory")
    private String postId;

    @NotBlank(message = "Comment text is mandatory")
    private String text;
    private String parentCommentId; // For nested replies

}
