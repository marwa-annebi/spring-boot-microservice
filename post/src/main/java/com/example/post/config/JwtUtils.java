//package com.example.post.config;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//
//@Component
//public class JwtUtils {
//
//    private final SecretKey secretKey;
//
//    public JwtUtils(@Value("${jwt.secret}") String secret) {
//        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes()); // Génère une clé forte
//    }
//
//    public String getUserIdFromToken(String token) {
//        if (token.startsWith("Bearer ")) {
//            token = token.substring(7); // Supprimer "Bearer " du token
//        }
//
//        Claims claims = Jwts.parserBuilder()
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//
//        return claims.getSubject(); // Retourne l'ID utilisateur (souvent "sub")
//    }
//}
