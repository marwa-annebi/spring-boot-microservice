package com.example.Comment.Service.dtos;

import com.example.Comment.Service.models.Post;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
@Data
@Builder
public class CommentWithPostDetails {

    private ObjectId id; // Comment ID
    private ObjectId postId; // The post ID (foreign key)
    private ObjectId userId; // The user ID (foreign key)
    private String text; // The content of the comment

    private Post postDetails;
}
