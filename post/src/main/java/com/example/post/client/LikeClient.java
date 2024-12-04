package com.example.post.client;

import com.example.post.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "like-service", url = "${application.config.likes-url}", configuration = FeignConfig.class)
public interface LikeClient {

    /**
     * Fetch the like counts for a list of posts.
     *
     * @param postIds List of post IDs.
     * @return Map of post IDs to their like counts.
     */
    @PostMapping("/api/like/count")
    Map<String, Integer> getLikesCount(@RequestBody List<String> postIds);

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
}
