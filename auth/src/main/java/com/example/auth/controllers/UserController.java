package com.example.auth.controllers;

import com.example.auth.models.User;
import com.example.auth.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public Principal currentUserName(Principal principal) {

        System.out.println(principal);
        return principal;
    }
    @GetMapping("/details/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/list")
    public List<User> getUserDetails(@RequestBody List<String> userIds) {
        return userService.getUserDetailsByIds(userIds);
    }
}
