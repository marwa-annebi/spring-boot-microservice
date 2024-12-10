package com.example.notification.client;


//import com.example.notification.config.FeignConfig;
//import com.example.notification.models.Post;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//@FeignClient(name = "post-service", url = "${application.config.posts-url}", configuration = FeignConfig.class)
//public interface PostClient {
//    @GetMapping("/api/post/{id}")
//    Post getPostById(@PathVariable("id") String id);
//}
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PostClient {
    private final RestTemplate restTemplate;

    public PostClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getPostTitleById(String postId) {
        return restTemplate.getForObject("http://post/api/post/" + postId + "/title", String.class);
    }
}