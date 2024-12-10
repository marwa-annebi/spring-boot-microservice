package com.example.post.client;

import com.example.post.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "comment-service", url = "${application.config.comments-url}", configuration = FeignConfig.class)
public interface CommentClient {
    @GetMapping("/api/comments/{postId}/count")
    Map<String, Long> getCommentCountByPostId(@PathVariable("postId") String postId);
}