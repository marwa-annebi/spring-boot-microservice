package com.example.Like.Service.client;

import com.example.Like.Service.config.FeignConfig;
import com.example.Like.Service.models.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service", url = "${application.config.posts-url}", configuration = FeignConfig.class)
public interface PostClient {
    @GetMapping("/api/post/{id}")
    Post getPostById(@PathVariable("id") String id);
}
