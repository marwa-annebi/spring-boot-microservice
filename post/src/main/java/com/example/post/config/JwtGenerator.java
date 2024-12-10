package com.example.post.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtGenerator {

    private final SecretKey secretKey;

    public JwtGenerator(String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId) // L'ID de l'utilisateur
                .setIssuedAt(new Date()) // Date de création
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 heure
                .signWith(secretKey, SignatureAlgorithm.HS256) // Signature forte
                .compact();
    }
}
