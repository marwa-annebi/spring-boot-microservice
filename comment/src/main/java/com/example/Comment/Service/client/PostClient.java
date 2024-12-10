package com.example.Comment.Service.client;

import com.example.Comment.Service.config.FeignConfig;
import com.example.Comment.Service.models.Post;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service", url = "${application.config.posts-url}", configuration = FeignConfig.class)
public interface PostClient {
//    @GetMapping("/api/post/{id}")
//    Post getPostById(@PathVariable("id") String id);
//}
@GetMapping("/api/post/{id}")
default Post getPostById(@PathVariable("id") String id) {
    try {
        return getPostByIdInternal(id);
    } catch (FeignException e) {
        throw new IllegalArgumentException("Post not found");
    }
}

    @GetMapping("/api/post/{id}")
    Post getPostByIdInternal(@PathVariable("id") String id);
}