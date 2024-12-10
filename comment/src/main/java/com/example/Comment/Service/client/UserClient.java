package com.example.Comment.Service.client;

import com.example.Comment.Service.config.FeignConfig;
import com.example.Comment.Service.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", url = "${application.config.users-url}", configuration = FeignConfig.class)
public interface UserClient {
    @GetMapping("/api/user/{email}")
    User findByEmail(@PathVariable("email") String email);

    @PostMapping("/api/user/list")
    List<User> getUserDetails(@RequestBody List<String> userIds);
}

