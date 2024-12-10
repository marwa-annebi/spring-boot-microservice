package com.example.post.client;

import com.example.post.config.FeignConfig;
import com.example.post.dtos.UserDto;
import com.example.post.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${application.config.users-url}", configuration = FeignConfig.class)
public interface UserClient {
    @GetMapping("/api/user/{email}")
    User findByEmail(@PathVariable("email") String email);

    @GetMapping("/api/user/details/{id}")
    User getUserById(@PathVariable("id") String id);
}

