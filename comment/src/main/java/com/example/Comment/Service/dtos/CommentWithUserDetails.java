package com.example.Comment.Service.dtos;

import com.example.Comment.Service.models.Comment;
import com.example.Comment.Service.models.Post;
import com.example.Comment.Service.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CommentWithUserDetails {
    private CommentDto comment;
    private User userDetails;
    private List<CommentWithUserDetails> replies;

    // Constructor
    public CommentWithUserDetails(CommentDto comment, User userDetails) {
        this.comment = comment;
        this.userDetails = userDetails;
    }

    // Getters and setters
    public CommentDto getComment() {
        return comment;
    }

    public void setComment(CommentDto comment) {
        this.comment = comment;
    }

    public User getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(User userDetails) {
        this.userDetails = userDetails;
    }
}
