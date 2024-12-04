package com.example.Like.Service.client;

import com.example.Like.Service.config.FeignConfig;
import com.example.Like.Service.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${application.config.users-url}", configuration = FeignConfig.class)
public interface UserClient {
    @GetMapping("/api/user/{email}")
    User findByEmail(@PathVariable("email") String email);
}

