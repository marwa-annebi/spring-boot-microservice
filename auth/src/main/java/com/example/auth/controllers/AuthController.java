package com.example.auth.controllers;


import com.example.auth.config.UserAuthenticationProvider;
import com.example.auth.dtos.*;
import com.example.auth.models.User;
import com.example.auth.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class AuthController {
    private final UserService userService;
    private final UserAuthenticationProvider userAuthenticationProvider;

    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public JwtResponse login(@RequestBody @Valid UserLoginDto credentialsDto) {
        UserDto userDto = userService.login(credentialsDto);
        userDto.setToken(userAuthenticationProvider.createToken(userDto.getEmail()));
        userDto.setRefreshToken(userAuthenticationProvider.generateRefreshToken(userDto.getEmail()));
        return JwtResponse.builder()
                .accessToken(userDto.getToken())
                .refreshToken(userDto.getRefreshToken()).build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid CreateUserDto user) {
        System.out.println("Register endpoint hit with payload: " + user);
        UserDto createdUser = userService.register(user);
        createdUser.setToken(userAuthenticationProvider.createToken(user.getEmail()));
        return ResponseEntity.created(URI.create("/users/" + createdUser.getId())).body(createdUser);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            // Decode refresh token to get user information
            String refreshToken = refreshTokenRequest.getToken();
            User userDto = userAuthenticationProvider.decodeRefreshToken(refreshToken);

            // Create a new access token for the user
            String accessToken = userAuthenticationProvider.createToken(userDto.getEmail());

            // Generate a new refresh token
            String newRefreshToken = userAuthenticationProvider.generateRefreshToken(userDto.getEmail());

            // Return the new access token along with the new refresh token
            return ResponseEntity.ok(JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(newRefreshToken)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(JwtResponse.builder()
                            .message("Invalid or expired refresh token")
                            .build());
        }
    }
}



