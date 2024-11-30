package com.example.post.client;

import com.example.post.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${application.config.users-url}")
public interface UserClient {
    @GetMapping("/users/{email}")
    User findByEmail(@PathVariable("email") String email);
}
