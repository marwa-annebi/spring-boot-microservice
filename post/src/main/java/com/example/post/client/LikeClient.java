package com.example.post.client;

import com.example.post.config.FeignConfig;
import com.example.post.dtos.LikeDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
//@FeignClient(name = "like-service", configuration = FeignConfig.class)

@FeignClient(name = "like-service", url = "${application.config.likes-url}", configuration = FeignConfig.class)
public interface LikeClient {

    @PostMapping("/api/like/count")
    ResponseEntity<Integer> getLikesCount(@RequestBody List<String> ids);
    /**
     * Fetch the dislike counts for a list of posts.
     *
     * @param postIds List of post IDs.
     * @return Map of post IDs to their dislike counts.
     */
    @PostMapping("/api/like/dislike/count")
    Map<String, Integer> getDislikesCount(@RequestBody List<String> postIds);

    /**
     * Check if the user liked specific posts (user extracted from token).
     *
     * @param postIds List of post IDs.
     * @return Map of post IDs to boolean values indicating if the user liked the posts.
     */
    @PostMapping("/api/like/user")
    Map<String, Boolean> getUserLikes(@RequestBody List<String> postIds);

    /**
     * Check if the user disliked specific posts (user extracted from token).
     *
     * @param postIds List of post IDs.
     * @return Map of post IDs to boolean values indicating if the user disliked the posts.
     */
    @PostMapping("/api/like/dislike/user")
    Map<String, Boolean> getUserDislikes(@RequestBody List<String> postIds);

    @GetMapping("/post/{postId}")
    List<LikeDetailsDto> getLikeDetails(@PathVariable String postId);
}
