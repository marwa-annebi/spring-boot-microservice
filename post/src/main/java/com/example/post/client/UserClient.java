package com.example.post.client;

import com.example.post.config.FeignConfig;
import com.example.post.dtos.UserDto;
import com.example.post.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", url = "${application.config.users-url}", configuration = FeignConfig.class)
public interface UserClient {
    @GetMapping("/api/user/{email}")
    User findByEmail(@PathVariable("email") String email);

    @GetMapping("/api/user/details/{id}")
    User getUserById(@PathVariable("id") String id);

    @PostMapping("/users/details")
    List<UserDetails> getUserDetails(@RequestBody List<String> userIds);
}

