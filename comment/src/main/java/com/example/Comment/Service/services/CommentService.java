package com.example.Comment.Service.services;

import com.example.Comment.Service.dtos.CreateCommentDto;
import com.example.Comment.Service.models.Comment;
import com.example.Comment.Service.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment addComment(CreateCommentDto createCommentDto) {
        Comment comment = Comment.builder()
                .postId(createCommentDto.getPostId())
                .userId(createCommentDto.getUserId())
                .text(createCommentDto.getText())
                .build();
        return commentRepository.save(comment);
    }

//    public List<Comment> getCommentsByPostId(String postId) {
//        return commentRepository.findAll()
//                .stream()
//                .filter(comment -> postId.equals(comment.getPostId()))
//                .toList();
//    }
public List<Comment> getCommentsByPostId(String postId) {
    return commentRepository.findByPostId(postId);
}
    public void deleteComment(String commentId) {
        commentRepository.deleteById(commentId);
    }
}