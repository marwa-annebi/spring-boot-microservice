package com.example.auth.controllers;


import com.example.auth.config.UserAuthenticationProvider;
import com.example.auth.dtos.*;
import com.example.auth.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    private final UserService userService;

    private final UserAuthenticationProvider userAuthenticationProvider;
    @CrossOrigin(origins = "http://localhost:3000")
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
    public JwtResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {

        // Decode refresh token to get user information
        String refreshToken = refreshTokenRequest.getToken();
        UserDto userDto = userAuthenticationProvider.decodeRefreshToken(refreshToken);

        // Create a new access token for the user
        String accessToken = userAuthenticationProvider.createToken(userDto.getEmail());

        // Generate a new refresh token
        String newRefreshToken = userAuthenticationProvider.generateRefreshToken(userDto.getEmail());

        // Return the new access token along with the new refresh token
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

}



