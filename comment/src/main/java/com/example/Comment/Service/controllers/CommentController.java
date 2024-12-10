package com.example.Comment.Service.controllers;

import com.example.Comment.Service.dtos.CommentWithUserDetails;
import com.example.Comment.Service.dtos.CreateCommentDto;
import com.example.Comment.Service.dtos.CreateReplyDto;
import com.example.Comment.Service.models.Comment;
import com.example.Comment.Service.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    private final CommentService commentService;


    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/count")
    public ResponseEntity<Map<String, Long>> getCommentCounts(@RequestBody List<String> postIds) {
        System.out.println("postIds"+postIds);
        Map<String, Long> commentCounts = commentService.getCommentCounts(postIds);
        return ResponseEntity.ok(commentCounts);
    }
    @PostMapping("/add")
    public ResponseEntity<Comment> addComment(@RequestBody @Valid CreateCommentDto createCommentDto) {
        Comment savedComment = commentService.addComment(createCommentDto);
        return ResponseEntity.status(201).body(savedComment);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentWithUserDetails>> getCommentsByPostId(@PathVariable String postId) {
        List<CommentWithUserDetails> comments = commentService.getCommentsWithUserDetails(postId);
        return ResponseEntity.ok(comments);
    }
    @PostMapping("/reply")
    public ResponseEntity<Comment> addReply(
            @Validated @RequestBody CreateReplyDto createReplyDto) {

        Comment reply = commentService.addReply(createReplyDto);
        return ResponseEntity.ok(reply);
    }
//    @PostMapping("/count")
//    public ResponseEntity<Map<String, Long>> getCommentCountsByPosts(@RequestBody List<String> postIds) {
//        Map<String, Long> commentCounts = commentService.getCommentCountsByPosts(postIds);
//        return ResponseEntity.ok(commentCounts);
//    }
//    @PostMapping("/count")
//    public ResponseEntity<Map<String, Long>> getCommentCounts(@RequestBody List<String> postIds) {
//        Map<String, Long> commentCounts = commentService.getCommentCounts(postIds);
//        return ResponseEntity.ok(commentCounts);
//    }
//    @PostMapping("/add")
//    public ResponseEntity<Comment> addComment(@RequestBody @Valid CreateCommentDto comment) {
//        Comment savedComment = commentService.addComment(comment);
//        return ResponseEntity.status(201).body(savedComment);
//    }
//@GetMapping("/{postId}/details")
//public ResponseEntity<Map<String, Object>> getPostDetails(@PathVariable String postId) {
//    Map<String, Object> details = commentService.getPostReactionsAndComments(postId);
//    return ResponseEntity.ok(details);
//}
@PostMapping("/post/comments")
public ResponseEntity<Map<String, List<Comment>>> getCommentsForPosts(@RequestBody List<String> postIds) {
    Map<String, List<Comment>> commentsMap = commentService.getCommentsForPosts(postIds);
    return ResponseEntity.ok(commentsMap);
}

}