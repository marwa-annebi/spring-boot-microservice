package com.example.auth.controllers;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/.well-known")
public class JwksController {
    @GetMapping("/jwks.json")
    public Map<String, Object> jwks() {
        Algorithm algorithm = Algorithm.HMAC256("your-secret-key");
        return Map.of(
                "keys", List.of(Map.of(
                        "kty", "oct",
                        "k", Base64.getEncoder().encodeToString("your-secret-key".getBytes()),
                        "alg", "HS256"
                ))
        );
    }
}
